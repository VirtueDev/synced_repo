//
// $Id$

package com.threerings.msoy.money.server;

import com.google.inject.Inject;
import com.threerings.msoy.money.data.all.MoneyType;
import com.threerings.msoy.money.gwt.HistoryListResult;
import com.threerings.msoy.money.gwt.MoneyService;
import com.threerings.msoy.money.server.persist.MoneyRepository;
import com.threerings.msoy.server.persist.MemberRecord;
import com.threerings.msoy.web.data.ServiceCodes;
import com.threerings.msoy.web.data.ServiceException;
import com.threerings.msoy.web.server.MsoyServiceServlet;

/**
 * Provides the server implementation of {@link MoneyService}.
 */
public class MoneyServlet extends MsoyServiceServlet
    implements MoneyService
{
    public HistoryListResult getTransactionHistory (final int memberId, final MoneyType type,
                                                    final int from, final int count)
        throws ServiceException
    {
        final MemberRecord mrec = requireAuthedUser();
        if (mrec.memberId != memberId && !mrec.isSupport()) {
            throw new ServiceException(ServiceCodes.E_ACCESS_DENIED);
        }

        final HistoryListResult ofTheJedi = new HistoryListResult();
        ofTheJedi.history = _moneyLogic.getLog(memberId, type, null, from, count, true);
        ofTheJedi.totalCount = _moneyLogic.getHistoryCount(memberId, type, null);
        return ofTheJedi;
    }

    @Inject protected MoneyLogic _moneyLogic;
    @Inject protected MoneyRepository _moneyRepo;
}
