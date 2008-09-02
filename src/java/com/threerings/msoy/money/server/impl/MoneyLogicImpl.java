//
// $Id$

package com.threerings.msoy.money.server.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.samskivert.io.PersistenceException;
import com.threerings.msoy.data.UserAction;
import com.threerings.msoy.data.UserActionDetails;
import com.threerings.msoy.data.all.MemberName;
import com.threerings.msoy.item.data.all.Item;
import com.threerings.msoy.item.data.all.ItemIdent;
import com.threerings.msoy.money.data.all.MemberMoney;
import com.threerings.msoy.money.data.all.MoneyHistory;
import com.threerings.msoy.money.data.all.MoneyType;
import com.threerings.msoy.money.data.all.TransactionType;
import com.threerings.msoy.money.server.MoneyConfiguration;
import com.threerings.msoy.money.server.MoneyLogic;
import com.threerings.msoy.money.server.MoneyResult;
import com.threerings.msoy.money.server.NotEnoughMoneyException;
import com.threerings.msoy.money.server.NotSecuredException;
import com.threerings.msoy.money.server.persist.MemberAccountHistoryRecord;
import com.threerings.msoy.money.server.persist.MemberAccountRecord;
import com.threerings.msoy.money.server.persist.MoneyRepository;
import com.threerings.msoy.money.server.persist.PersistentMoneyType;
import com.threerings.msoy.money.server.persist.PersistentTransactionType;
import com.threerings.msoy.money.server.persist.RepositoryException;
import com.threerings.msoy.money.server.persist.StaleDataException;
import com.threerings.msoy.server.MsoyEventLogger;
import com.threerings.msoy.server.persist.UserActionRepository;

/**
 * Default implementation of the money service.
 *
 * TODO: Transactional support
 *
 * @author Kyle Sampson <kyle@threerings.net>
 */
@Singleton
public class MoneyLogicImpl
    implements MoneyLogic
{
    @Inject
    public MoneyLogicImpl (
        final MoneyRepository repo, final EscrowCache escrowCache,
        final MoneyHistoryExpirer expirer, final UserActionRepository userActionRepo,
        final MsoyEventLogger eventLog)
    {
        _repo = repo;
        _escrowCache = escrowCache;
        _userActionRepo = userActionRepo;
        _eventLog = eventLog;
        _expirer = expirer;
    }

    @Retry(exception=StaleDataException.class)
    public MoneyResult awardCoins (
        final int memberId, final int creatorId, final int affiliateId,
        final ItemIdent item, final int amount, final String description, final
        UserAction userAction)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId), "Cannot award coins to guests.");
        Preconditions.checkArgument(amount >= 0, "amount is invalid: %d", amount);
        Preconditions.checkArgument(item == null || item.itemId != 0 || item.type != 0,
            "item is invalid: %s", (item == null ? null : item.toString()));

        MemberAccountRecord account = _repo.getAccountById(memberId);
        if (account == null) {
            account = new MemberAccountRecord(memberId);
        }
        final MemberAccountHistoryRecord history = account.awardCoins(amount, item, description);
        _repo.saveAccount(account);
        _repo.addHistory(history);

        // TODO: creator and affiliate

        logUserAction(memberId, UserActionDetails.INVALID_ID, userAction, item, description);
        final UserActionDetails info = logUserAction(memberId, 0, userAction, item, description);
        logInPanopticon(info, MoneyType.COINS, amount, account);
        
        return new MoneyResult(account.getMemberMoney(), null, null, 
            history.createMoneyHistory(null), null, null);
    }

    @Retry(exception=StaleDataException.class)
    public MoneyResult buyBars (final int memberId, final int numBars, final String description)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId), "Guests cannot buy bars.");
        Preconditions.checkArgument(numBars >= 0, "numBars is invalid: %d", numBars);

        MemberAccountRecord account = _repo.getAccountById(memberId);
        if (account == null) {
            account = new MemberAccountRecord(memberId);
        }
        final MemberAccountHistoryRecord history = account.buyBars(numBars, description);
        _repo.saveAccount(account);
        _repo.addHistory(history);

        logUserAction(memberId, UserActionDetails.INVALID_ID, UserAction.BOUGHT_BARS, null,
            history.getDescription());

        return new MoneyResult(account.getMemberMoney(), null, null, 
            history.createMoneyHistory(null), null, null);
    }

    @Retry(exception=StaleDataException.class)
    public MoneyResult buyItemWithBars (
        final int memberId, final ItemIdent item, final boolean support)
        throws NotEnoughMoneyException, NotSecuredException
    {
        return buyItem(memberId, item, MoneyType.BARS, support);
    }

    @Retry(exception=StaleDataException.class)
    public MoneyResult buyItemWithCoins (
        final int memberId, final ItemIdent item, final boolean support)
        throws NotEnoughMoneyException, NotSecuredException
    {
        return buyItem(memberId, item, MoneyType.COINS, support);
    }

    public void deductBling (final int memberId, final double amount)
    {
        // TODO Auto-generated method stub
    }

    public int exchangeBlingForBars (final int memberId, final double blingAmount)
        throws NotEnoughMoneyException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public BigDecimal getBlingWorth (final int memberId)
    {
        // TODO Auto-generated method stub
        return null;
    }

    protected static EnumSet<PersistentTransactionType> toPersist (
        final EnumSet<TransactionType> transactionTypes)
    {
        if (transactionTypes == null) {
            return EnumSet.allOf(PersistentTransactionType.class);
        } else {
            EnumSet<PersistentTransactionType> persistTransactionTypes =
                EnumSet.noneOf(PersistentTransactionType.class);
            for (TransactionType transactionType : transactionTypes) {
                persistTransactionTypes.add(
                    PersistentTransactionType.fromTransactionType(transactionType));
            }

            return persistTransactionTypes;
        }
    }

    public List<MoneyHistory> getLog (
        final int memberId, final MoneyType type, final EnumSet<TransactionType> transactionTypes,
        final int start, final int count, final boolean descending)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId),
            "Cannot retrieve money log for guests.");
        Preconditions.checkArgument(start >= 0, "start is invalid: %d", start);
        Preconditions.checkArgument(count > 0, "count is invalid: %d", count);

        final List<MemberAccountHistoryRecord> records = _repo.getHistory(memberId, 
            PersistentMoneyType.fromMoneyType(type), toPersist(transactionTypes), start, count, 
            descending);
        
        // Put all records into a map by their ID.  We'll use this map to get a set of history ID's
        // that we currently have.
        final Map<Integer, MoneyHistory> referenceMap = new HashMap<Integer, MoneyHistory>();
        for (final MemberAccountHistoryRecord record : records) {
            referenceMap.put(record.id, record.createMoneyHistory(null));
        }
        
        // Create a set of reference transaction IDs we don't already have.  We'll look these up.
        final Set<Integer> lookupRefIds = new HashSet<Integer>();
        for (final MemberAccountHistoryRecord record : records) {
            if (record.referenceTxId != 0 && !referenceMap.keySet().contains(record.referenceTxId)) {
                lookupRefIds.add(record.referenceTxId);
            }
        }
        if (lookupRefIds.size() > 0) {
            for (final MemberAccountHistoryRecord record : _repo.getHistory(lookupRefIds)) {
                referenceMap.put(record.id, record.createMoneyHistory(null));
            }
        }
        
        // Now create the money histories, using the reference map for the references as necessary.
        final List<MoneyHistory> log = new ArrayList<MoneyHistory>();
        for (final MemberAccountHistoryRecord record : records) {
            log.add(record.createMoneyHistory(record.referenceTxId == 0 ? null : 
                referenceMap.get(record.referenceTxId)));
        }
        
        return log;
    }

    public int getHistoryCount (
        final int memberId, final MoneyType type, final EnumSet<TransactionType> transactionTypes)
    {
        return _repo.getHistoryCount(memberId, PersistentMoneyType.fromMoneyType(type),
            toPersist(transactionTypes));
    }

    public MoneyConfiguration getMoneyConfiguration ()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public MemberMoney getMoneyFor (final int memberId)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId),
            "Cannot retrieve money info for guests.");

        final MemberAccountRecord account = _repo.getAccountById(memberId);
        return account != null ? account.getMemberMoney() : new MemberMoney(memberId);
    }

    public int secureBarPrice (
        final int memberId, final int creatorId, final int affiliateId,
        final ItemIdent item, final int numBars, final String description)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId), "Guests cannot secure prices.");
        Preconditions.checkArgument(!MemberName.isGuest(creatorId), "Creators cannot be guests.");
        Preconditions.checkArgument(item != null && (item.type != 0 || item.itemId != 0),
            "item is invalid: %s", item.toString());
        Preconditions.checkArgument(numBars >= 0, "bars is invalid: %d", numBars);

        // TODO: Use exchange rate to calculate coins.
        final PriceQuote quote = new PriceQuote(MoneyType.BARS, 0, numBars);
        final PriceKey key = new PriceKey(memberId, item);
        final Escrow escrow = new Escrow(creatorId, affiliateId, description, quote);
        _escrowCache.addEscrow(key, escrow);
        return 0;
    }

    public int secureCoinPrice (
        final int memberId, final int creatorId, final int affiliateId,
        final ItemIdent item, final int numCoins, final String description)
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId), "Guests cannot secure prices.");
        Preconditions.checkArgument(!MemberName.isGuest(creatorId), "Creators cannot be guests.");
        Preconditions.checkArgument(item != null && (item.type != 0 || item.itemId != 0),
            "item is invalid: %s", item.toString());
        Preconditions.checkArgument(numCoins >= 0, "numCoins is invalid: %d", numCoins);

        // TODO: Use exchange rate to calculate bars.
        final PriceQuote quote = new PriceQuote(MoneyType.COINS, numCoins, 0);
        final PriceKey key = new PriceKey(memberId, item);
        final Escrow escrow = new Escrow(creatorId, affiliateId, description, quote);
        _escrowCache.addEscrow(key, escrow);
        return 0;
    }

    public void updateMoneyConfiguration (final MoneyConfiguration config)
    {
        // TODO Auto-generated method stub
    }

    public void init ()
    {
        _expirer.start();
    }

    private MoneyResult buyItem (
        final int memberId, final ItemIdent item, final MoneyType purchaseType,
        final boolean support)
        throws NotEnoughMoneyException, NotSecuredException
    {
        Preconditions.checkArgument(!MemberName.isGuest(memberId), "Guests cannot buy items.");
        Preconditions.checkArgument(item != null && (item.type != 0 || item.itemId != 0),
            "item is invalid: %s", item.toString());
        Preconditions.checkArgument(purchaseType == MoneyType.BARS ||
            purchaseType == MoneyType.COINS, "purchaseType is invalid: %s",
            purchaseType.toString());

        // Get the secured prices for the item.
        final PriceKey key = new PriceKey(memberId, item);
        final Escrow escrow = _escrowCache.getEscrow(key);
        if (escrow == null) {
            throw new NotSecuredException(memberId, item);
        }
        final PriceQuote quote = escrow.getQuote();
        // TODO: MUCH TODO
        int amount = purchaseType == MoneyType.BARS ? quote.getBars() : quote.getCoins();

        // If the creator is buying their own item, don't give them a payback, and deduct the amount
        // they would have received.
        boolean payCreator = true;
        if (memberId == escrow.getCreatorId()) {
            amount -= (int)(0.3 * amount);
            payCreator = false;
        }

        // Get buyer account and make sure they can afford the item.
        final MemberAccountRecord account = _repo.getAccountById(memberId);
        if (account == null || (!support && !account.canAfford(amount, purchaseType))) {
            final int available = (account == null ? 0 : (purchaseType == MoneyType.BARS ?
                account.getBars() : account.getCoins()));
            throw new NotEnoughMoneyException(available, amount, purchaseType, memberId);
        }

        // Get creator.
        MemberAccountRecord creator;
        if (memberId == escrow.getCreatorId()) {
            creator = account;
        } else {
            creator = _repo.getAccountById(escrow.getCreatorId());
            if (creator == null) {
                creator = new MemberAccountRecord(escrow.getCreatorId());
            }
        }

        // Update the member account
        final MemberAccountHistoryRecord history = account.buyItem(amount, purchaseType,
            escrow.getDescription(), item, support);
        _repo.addHistory(history);
        _repo.saveAccount(account);
        UserActionDetails info = logUserAction(memberId, UserActionDetails.INVALID_ID,
            UserAction.BOUGHT_ITEM, item, escrow.getDescription());
        logInPanopticon(info, purchaseType, history.getSignedAmount(), account);

        // Update the creator account, if they get a payment.
        MemberAccountHistoryRecord creatorHistory = history;
        if (payCreator) {
            creatorHistory = creator.creatorPayout((int)history.getAmount(), quote.getListedType(),
                "Item purchased: " + escrow.getDescription(), item, 0.3, history.id);
            _repo.addHistory(creatorHistory);
            _repo.saveAccount(creator);
            info = logUserAction(escrow.getCreatorId(), memberId, UserAction.RECEIVED_PAYOUT, item,
                escrow.getDescription());
            logInPanopticon(info, purchaseType, creatorHistory.getSignedAmount(), creator);
        }

        // TODO: update affiliate with some amount of bling.

        // The item no longer needs to be in the cache.
        _escrowCache.removeEscrow(key);

        final MoneyHistory mh = history.createMoneyHistory(null);
        final MoneyHistory creatorMH = creatorHistory.createMoneyHistory(mh);
        return new MoneyResult(account.getMemberMoney(), payCreator ? creator.getMemberMoney() :
            null, null, mh, payCreator ? creatorMH : null, null);
    }

    private void logInPanopticon (
        final UserActionDetails info, final MoneyType type,
        final double delta, final MemberAccountRecord account)
    {
        if (type == MoneyType.COINS) {
            _eventLog.flowTransaction(info, (int)delta, account.getCoins());
        } else if (type == MoneyType.BARS) {
            // TODO
        } else {
            // TODO: bling
        }
    }

    private UserActionDetails logUserAction (
        final int memberId, final int otherMemberId, final UserAction userAction,
        final ItemIdent item, final String description)
    {
        try {
            final UserActionDetails details = new UserActionDetails(
                memberId, userAction, otherMemberId,
                (item == null) ? Item.NOT_A_TYPE : item.type,
                (item == null) ? UserActionDetails.INVALID_ID : item.itemId,
                description);
            _userActionRepo.logUserAction(details);
            return details;
        } catch (final PersistenceException pe) {
            throw new RepositoryException(pe);
        }
    }

    private final MoneyHistoryExpirer _expirer;
    private final MsoyEventLogger _eventLog;
    private final UserActionRepository _userActionRepo;
    private final MoneyRepository _repo;
    private final EscrowCache _escrowCache;
}
