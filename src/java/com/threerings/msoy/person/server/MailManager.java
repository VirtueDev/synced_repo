//
// $Id$

package com.threerings.msoy.person.server;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.RepositoryListenerUnit;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Tuple;

import com.threerings.msoy.web.data.MailFolder;
import com.threerings.msoy.web.data.MailHeaders;
import com.threerings.msoy.web.data.MailMessage;
import com.threerings.msoy.web.data.MailPayload;
import com.threerings.msoy.web.data.MemberName;

import com.threerings.msoy.server.JSONMarshaller;
import com.threerings.msoy.server.MsoyServer;
import com.threerings.msoy.server.persist.MemberRecord;
import com.threerings.msoy.server.persist.MemberRepository;

import com.threerings.msoy.data.MemberObject;
import com.threerings.msoy.person.server.persist.MailFolderRecord;
import com.threerings.msoy.person.server.persist.MailMessageRecord;
import com.threerings.msoy.person.server.persist.MailRepository;

/**
 * Manage msoy mail.
 */
public class MailManager
{
    /**
     * Prepares our mail manager for operation.
     */
    public void init (ConnectionProvider conProv, MemberRepository memberRepo)
    {
        _mailRepo = new MailRepository(conProv);
        _memberRepo = memberRepo;
    }

    /**
     * Returns a reference to our repository.
     */
    public MailRepository getRepository ()
    {
        return _mailRepo;
    }

    /**
     * Fetch and return a single message from the database. 
     */
    public void getMessage (final int memberId, final int folderId, final int messageId,
                            final boolean flagAsRead, ResultListener<MailMessage> waiter)
    {
        final MemberObject mObj =
                folderId == MailFolder.INBOX_FOLDER_ID ? MsoyServer.lookupMember(memberId) : null;
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<MailMessage>(waiter) {
            public MailMessage invokePersistResult () throws PersistenceException {
                MailMessageRecord record = _mailRepo.getMessage(memberId, folderId, messageId);
                if (record == null) {
                    return null;
                }
                if (record.unread && flagAsRead) {
                    _mailRepo.setUnread(memberId, folderId, messageId, false);
                    // are we logged in, and did we read an unread message in the inbox?
                    if (mObj != null) {
                        // if so, count how many more of those there are
                        _count = _mailRepo.getMessageCount(memberId, folderId);
                    }
                }
                return toMailMessage(record);
            }
            public void handleSuccess () {
                if (_count != null) {
                    mObj.setHasNewMail(_count.right > 0);
                }
                super.handleSuccess();
            }
            protected Tuple<Integer, Integer> _count;
        });
    }

    /**
     * Fetch and return all the messages in a folder from the database. 
     */
    public void getHeaders (final int memberId, final int folderId,
                             ResultListener<List<MailHeaders>> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<List<MailHeaders>>(waiter) {
            public List<MailHeaders> invokePersistResult () throws PersistenceException {
                List<MailHeaders> result = new ArrayList<MailHeaders>();
                for (MailMessageRecord record : _mailRepo.getMessages(memberId, folderId)) {
                    result.add(toMailHeaders(record));
                }
                return result;
            }
        });
    }

    /**
     * Fetch and return a single folder from the database. 
     */
    public void getFolder (final int memberId, final int folderId,
                           ResultListener<MailFolder> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<MailFolder>(waiter) {
            public MailFolder invokePersistResult () throws PersistenceException {
                return buildFolder(_mailRepo.getFolder(memberId, folderId));
            }
        });
    }

    /**
     * Fetch and return all of a given member's folders from the database.
     */
    public void getFolders (final int memberId, ResultListener<List<MailFolder>> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<List<MailFolder>>(waiter) {
            public List<MailFolder> invokePersistResult () throws PersistenceException {
                List<MailFolder> result = new ArrayList<MailFolder>();
                for (MailFolderRecord record : _mailRepo.getFolders(memberId)) {
                    result.add(buildFolder(record));
                }
                return result;
            }
        });
    }


    /**
     * Overwrite the serialized state of a message's payload with the new supplied data.
     */
    public void updatePayload (final int memberId, final int folderId, final int messageId,
                               final MailPayload payload, ResultListener<Void> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<Void>(waiter) {
            public Void invokePersistResult () throws PersistenceException {
                try {
                    byte[] state =
                        JSONMarshaller.getMarshaller(payload.getClass()).getStateBytes(payload);
                    _mailRepo.setPayloadState(memberId, folderId, messageId, state);
                } catch (Exception e) {
                    throw new PersistenceException(e);
                }
                return null;
            }
        });
    }

    /**
     * Deliver a message, i.e. file one copy of it in the sender's 'Sent' folder,
     * and one copy in the recipient's 'Inbox' folder.
     */    
    public void deliverMessage (final int senderId, final int recipientId, final String subject,
                                final String text, final MailPayload payload,
                                ResultListener<Void> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<Void>(waiter) {
            public Void invokePersistResult () throws PersistenceException {
                // copy the mail message into record format
                MailMessageRecord record = new MailMessageRecord();
                record.senderId = senderId;
                record.recipientId = recipientId;
                record.subject = subject;
                record.bodyText = text;
                
                if (payload != null) {
                    record.payloadType = payload.getType();
                    try {
                        record.payloadState =
                            JSONMarshaller.getMarshaller(payload.getClass()).getStateBytes(payload);
                    } catch (Exception e) {
                        throw new PersistenceException(e);
                    }
                }
                _mailRepo.deliverMessage(record);
                return null;
            }

            public void handleSuccess () {
                // if all went well and the recipient is online, notify them they have new mail
                MemberObject mObj = MsoyServer.lookupMember(recipientId);
                if (mObj != null) {
                    mObj.setHasNewMail(true);
                }
                super.handleSuccess();
            }
        });
    }

    /**
     * Move some messages from one folder to another.
     */
    public void moveMessages (final int memberId, final int folderId, final int[] msgIdArr,
                              final int newFolderId, ResultListener<Void> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<Void>(waiter) {
            public Void invokePersistResult () throws PersistenceException {
                _mailRepo.moveMessage(memberId, folderId, newFolderId, msgIdArr);
                return null;
            }
        });

    }
    
    /**
     * Bulk delete a number of messages from the database. Note: This actually
     * DELETES the messages, it doesn't move them to the Trash folder.
     */
    public void deleteMessages (final int memberId, final int folderId, final int[] msgIdArr,
                                ResultListener<Void> waiter)
    {
        MsoyServer.invoker.postUnit(new RepositoryListenerUnit<Void>(waiter) {
            public Void invokePersistResult () throws PersistenceException {
            	_mailRepo.deleteMessage(memberId, folderId, msgIdArr);
                return null;
            }
        });
    }

    // create a MailHeaders object from a a MailMessageRecord
    protected MailHeaders toMailHeaders (MailMessageRecord record)
        throws PersistenceException
    {
        MailHeaders headers = new MailHeaders();
        headers.messageId = record.messageId;
        headers.folderId = record.folderId;
        headers.ownerId = record.ownerId;
        headers.subject = record.subject;
        headers.sent = new Date(record.sent.getTime());
        headers.unread = record.unread;
        
        if (record.senderId != 0) {
            MemberRecord memRec = _memberRepo.loadMember(record.senderId);
            headers.sender = new MemberName(memRec.name, memRec.memberId);
        } else {
            // TODO: This should not be hard-coded here.
            headers.sender = new MemberName("System Administrators", 0);
        }

        MemberRecord memRec = _memberRepo.loadMember(record.recipientId);
        headers.recipient = new MemberName(memRec.name, memRec.memberId);
        return headers;
    }
    
    // convert a MailMessageRecord to a MailMessage
    @SuppressWarnings("unchecked")
    protected MailMessage toMailMessage (MailMessageRecord record)
        throws PersistenceException
    {
        MailMessage message = new MailMessage();
        message.headers = toMailHeaders(record);
        message.bodyText = record.bodyText;
        if (record.payloadType != 0) {
            if (record.payloadState != null) {
                try {
                    Class<? extends MailPayload> objectClass =
                        MailPayload.getPayloadClass(record.payloadType);
                    JSONMarshaller<? extends MailPayload> marsh =
                        JSONMarshaller.getMarshaller(objectClass);
                    message.payload = marsh.newInstance(record.payloadState);
                } catch (Exception e) {
                    throw new PersistenceException("Failed to unserialize message payload", e);
                }
            }
        }
        return message;
    }

    // convert a MailFolderRecord to its MailFolder form
    protected MailFolder toMailFolder (MailFolderRecord record)
        throws PersistenceException
    {
        MailFolder folder = new MailFolder();
        folder.folderId = record.folderId;
        folder.ownerId = record.ownerId;
        folder.name = record.name;
        return folder;
    }

    // build a MailFolder object, including the message counts which require a separate query
    protected MailFolder buildFolder (MailFolderRecord record) throws PersistenceException
    {
        MailFolder folder = toMailFolder(record);
        Tuple<Integer, Integer> counts = _mailRepo.getMessageCount(record.ownerId, record.folderId);
        folder.unreadCount = counts.right != null ? counts.right.intValue() : 0;
        folder.readCount = counts.left != null ? counts.left.intValue() : 0;
        return folder;
    }

    /** Provides access to persistent mail data. */
    protected MailRepository _mailRepo;
    
    /** Provides access to persistent member data. */
    protected MemberRepository _memberRepo;
}
