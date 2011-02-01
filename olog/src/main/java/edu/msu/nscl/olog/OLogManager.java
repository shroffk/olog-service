/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */
package edu.msu.nscl.olog;

import java.sql.Connection;
import java.util.Collection;
import java.util.Collections;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Central business logic layer that implements all directory operations.
 * 
 * @author Eric Berryman taken from Ralph Lange <Ralph.Lange@bessy.de>
 */
public class OLogManager {

    private static OLogManager instance = new OLogManager();

    /**
     * Create an instance of OlogManager
     */
    private OLogManager() {
    }

    /**
     * Returns the (singleton) instance of OlogManager
     *
     * @return the instance of OlogManager
     */
    public static OLogManager getInstance() {
        return instance;
    }

    /**
     * Merges XmlLogbooks and XmlTags of two logs in place
     *
     * @param dest destination log
     * @param src source log
     */
    public static void mergeXmlLogs(XmlLog dest, XmlLog src) {
        src_logbooks:
        for (XmlLogbook s : src.getXmlLogbooks().getLogbooks()) {
            for (XmlLogbook d : dest.getXmlLogbooks().getLogbooks()) {
                if (d.getName().equals(s.getName())) {
 //TODO: here                   d.setValue(s.getValue());
                    continue src_logbooks;
                }
            }
            dest.getXmlLogbooks().addXmlLogbook(s);
        }
        src_tags:
        for (XmlTag s : src.getXmlTags().getTags()) {
            for (XmlTag d : dest.getXmlTags().getTags()) {
                if (d.getName().equals(s.getName())) {
                    continue src_tags;
                }
            }
            dest.getXmlTags().addXmlTag(s);
        }
    }

    /**
     * Return single log found by log id.
     *
     * @param logId id to look for
     * @return XmlLog with found log and its logbooks
     * @throws CFException on SQLException
     */
    public XmlLog findLogById(int logId) throws CFException {
        return FindLogsQuery.findLogById(logId);
    }

    /**
     * Return logs found by matching tags against a collection of name patterns.
     *
     * @param matches collection of name patterns to match
     * @return XmlLogs container with all found logs and their properties
     * @throws CFException wrapping an SQLException
     */
    public XmlLogs findLogsByLogbookName(String name) throws CFException {
        return FindLogsQuery.findLogsByLogbookName(name);
    }

    /**
     * Returns logs found by matching logbook names, tag names, log subject/description.
     *
     * @param matches multivalued map of logbook, tag, log names and patterns to match
     * their values against.
     * @return XmlLogs container with all found logs and their logbooks
     * @throws CFException wrapping an SQLException
     */
    public XmlLogs findLogsByMultiMatch(MultivaluedMap<String, String> matches) throws CFException {
        return FindLogsQuery.findLogsByMultiMatch(matches);
    }

    /**
     * Deletes a log identified by <tt>logId</tt>.
     *
     * @param logId log to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeLog(int logId) throws CFException {
        DeleteLogQuery.deleteLogIgnoreNoexist(logId);
    }

    /**
     * Deletes a log identified by <tt>logId</tt>.
     *
     * @param logId log to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeExistingLog(int logId) throws CFException {
        DeleteLogQuery.deleteLogFailNoexist(logId);
    }

    /**
     * List all Logbooks in the database.
     *
     * @throws CFException wrapping an SQLException
     */
    public XmlLogbooks listLogbooks() throws CFException {
        return ListLogbooksQuery.getLogbooks();
    }

    /**
     * Return single logbook found by name.
     *
     * @param name name to look for
     * @return XmlLogbook with found logbook and its logs
     * @throws CFException on SQLException
     */
    public XmlLogbook findLogbookByName(String name) throws CFException {
        XmlLogbook p = ListLogbooksQuery.findLogbook(name);
        if (p != null) {
            XmlLogs c = FindLogsQuery.findLogsByLogbookName(name);
            if (c != null) {
                p.setXmlLogs(c);
            }
        }
        return p;
    }

    /**
     * Add the logbook identified by <tt>logbook</tt> to the logs
     * specified in the XmlLogs <tt>data</tt>.
     *
     * @param logbook logbook to add
     * @param data XmlLogbook data with all logs
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void updateLogbook(String logbook, XmlLogbook data) throws CFException {
        UpdateValuesQuery.updateLogbook(logbook, data);
    }

    /**
     * Adds the logbook identified by <tt>tag</tt> <b>exclusively</b>
     * to the logs specified in the XmlLogbook payload <tt>data</tt>, creating it
     * if necessary.
     *
     * @param logbook logbook to add
     * @param data XmlLogbook container with all logs to add logbook to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceLogbook(String logbook, XmlLogbook data) throws CFException {
        DeleteLogbookQuery.removeLogbook(logbook);
        CreateLogbookQuery.createLogbook(data.getName(), data.getOwner());
        UpdateValuesQuery.updateLogbook(data.getName(), data);
    }


    /**
     * Create or replace logbooks specified in <tt>data</tt>.
     *
     * @param data XmlLogbooks data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceLogbooks(XmlLogbooks data) throws CFException {
        for (XmlLogbook logbook : data.getLogbooks()) {
            removeLogbook(logbook.getName());
            createOrReplaceLogbook(logbook.getName(), logbook);
        }
    }

    /**
     * Add the logbook identified by <tt>logbook</tt>
     * to the single log <tt>id</tt>.
     *
     * @param logbook logbook to add
     * @param logId log to add the logbook to
     * @param data XmlLogbook
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addSingleLogbook(String logbook, int logId, XmlLogbook data) throws CFException {
        UpdateValuesQuery.updateLogbook(logbook, data);
    }

    /**
     * Deletes a logbook identified by <tt>name</tt> from all logs.
     *
     * @param logbook tag to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeLogbook(String logbook) throws CFException {
        DeleteLogbookQuery.removeLogbook(logbook);
    }

    /**
     * Deletes a logbook identified by <tt>name</tt> from all logs, failing if
     * the logbook does not exist.
     *
     * @param logbook tag to delete
     * @throws CFException wrapping an SQLException or on failure
     */
    public void removeExistingLogbook(String logbook) throws CFException {
        DeleteLogbookQuery.removeExistingLogbook(logbook);
    }

    /**
     * Deletes a logbook identified by <tt>name</tt> from a single log.
     *
     * @param logbook tag to delete
     * @param logId log to delete it from
     * @throws CFException wrapping an SQLException
     */
    public void removeSingleLogbook(String logbook, int logId) throws CFException {
        DeleteLogbookQuery.deleteOneValue(logbook, logId);
    }

    /**
     * List all tags in the database.
     *
     * @throws CFException wrapping an SQLException
     */
    public XmlTags listTags() throws CFException {
        return ListLogbooksQuery.getTags();
    }

    /**
     * Return single tag found by name.
     *
     * @param name name to look for
     * @return XmlTag with found tag and its logs/status
     * @throws CFException on SQLException
     */
    public XmlTag findTagByName(String name) throws CFException {
        XmlTag t = ListLogbooksQuery.findTag(name);
        if (t != null) {
            XmlLogs c = FindLogsQuery.findLogsByLogbookName(name);
            if (c != null) {
                t.setXmlLogs(c);
            }
        }
        return t;
    }

    /**
     * Add the tag identified by <tt>tag</tt> and <tt>status</tt> to the logs
     * specified in the XmlLogs <tt>data</tt>.
     *
     * @param tag tag to add
     * @param data XmlTag with list of all logs to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void updateTag(String tag, XmlTag data) throws CFException {
        UpdateValuesQuery.updateTag(tag, data);
    }

    /**
     * Adds the tag identified by <tt>tag</tt> <b>exclusively</b>
     * to the logs specified in the XmlTag payload <tt>data</tt>, creating it
     * if necessary.
     *
     * @param tag tag to add
     * @param data XmlTag container with all logs to add tag to
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceTag(String tag, XmlTag data) throws CFException {
        DeleteLogbookQuery.removeLogbook(tag);
        CreateLogbookQuery.createTag(data.getName());
        UpdateValuesQuery.updateTag(data.getName(), data);
    }

    /**
     * Create tags specified in <tt>data</tt>.
     *
     * @param data XmlTags data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceTags(XmlTags data) throws CFException {
        for (XmlTag tag : data.getTags()) {
            removeTag(tag.getName());
            createOrReplaceTag(tag.getName(), tag);
        }
    }

    /**
     * Add the tag identified by <tt>tag</tt> to the single log <tt>logId</tt>.
     *
     * @param tag tag to add
     * @param logId
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void addSingleTag(String tag, int logId) throws CFException {
        UpdateValuesQuery.updateTag(tag, logId);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from all logs.
     *
     * @param tag tag to delete
     * @throws CFException wrapping an SQLException
     */
    public void removeTag(String tag) throws CFException {
        DeleteLogbookQuery.removeLogbook(tag);
    }

    /**
     * Deletes a tag identified by <tt>name</tt> from a single log.
     *
     * @param tag tag to delete
     * @param logId log to delete it from
     * @throws CFException wrapping an SQLException
     */
    public void removeSingleTag(String tag, int logId) throws CFException {
        DeleteLogbookQuery.deleteOneValue(tag, logId);
    }

    /**
     * Update a log identified by <tt>logId</tt>, creating it when necessary.
     * The logbook set in <tt>data</tt> has to be complete, i.e. the existing
     * log logbooks are <b>replaced</b> with the logbooks in <tt>data</tt>.
     *
     * @param logId log to update
     * @param data XmlLog data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    public void createOrReplaceLog(int logId, XmlLog data) throws CFException {
        DeleteLogQuery.deleteLogIgnoreNoexist(logId);
        CreateLogQuery.createLog(data);
    }

    /**
     * Create logs specified in <tt>data</tt>.
     *
     * @param data XmlLogs data
     * @throws CFException on ownership mismatch, or wrapping an SQLException
     */
    public void createOrReplaceLogs(XmlLogs data) throws CFException {
        for (XmlLog log : data.getLogs()) {
            removeLog(log.getId());
            createOneLog(log);
        }
    }

    /**
     * Create a new log using the logbook set in <tt>data</tt>.
     *
     * @param data XmlLog data
     * @throws CFException on ownership or name mismatch, or wrapping an SQLException
     */
    private void createOneLog(XmlLog data) throws CFException {
        CreateLogQuery.createLog(data);
    }

    /**
     * Merge logbook set in <tt>data</tt> into the existing log <tt>logId</tt>.
     *
     * @param logId log to merge the logbooks and tags into
     * @param data XmlLog data containing logbooks and tags
     * @throws CFException on name or owner mismatch, or wrapping an SQLException
     */
    public void updateLog(int logId, XmlLog data) throws CFException {
        XmlLog dest = findLogById(logId);
        if (dest == null) {
            throw new CFException(Response.Status.NOT_FOUND,
                    "Specified log '" + logId
                    + "' does not exist");
        }
        dest.setId(data.getId());
        dest.setOwner(data.getOwner());
        mergeXmlLogs(dest, data);
        createOrReplaceLog(logId, dest);
    }

    /**
     * Check that <tt>logId</tt> matches the log id in <tt>data</tt>.
     *
     * @param logId log id to check
     * @param data XmlLog data to check against
     * @throws CFException on name mismatch
     */
    //TODO: fix this
    public void checkIdMatchesPayload(int logId, XmlLog data) throws CFException {
    //    if (!logId.equals(data.getId())) {
    //        throw new CFException(Response.Status.BAD_REQUEST,
    //                "Specified log id '" + logId
     //               + "' and payload log id '" + data.getId() + "' do not match");
     //   }
    }

    /**
     * Check the log in <tt>data</tt> for valid id/owner data.
     *
     * @param data XmlLog data to check
     * @throws CFException on error
     */
    public void checkValidIdAndOwner(XmlLog data) throws CFException {
        if (data.getId() == 0) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid log id (null or empty string)");
        }
        if (data.getOwner() == null || data.getOwner().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid log owner (null or empty string) for '" + data.getId() + "'");
        }
    }

    /**
     * Check all logs in <tt>data</tt> for valid id/owner data.
     *
     * @param data XmlLogs data to check
     * @throws CFException on error
     */
    public void checkValidIdAndOwner(XmlLogs data) throws CFException {
        if (data == null || data.getLogs() == null) return;
        for (XmlLog c : data.getLogs()) {
            checkValidIdAndOwner(c);
        }
    }

    /**
     * Check that <tt>name</tt> matches the tag name in <tt>data</tt>.
     *
     * @param name tag name to check
     * @param data XmlTag data to check against
     * @throws CFException on name mismatch
     */
    public void checkNameMatchesPayload(String name, XmlTag data) throws CFException {
        if (data == null) return;
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified tag name '" + name
                    + "' and payload tag name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check the tag in <tt>data</tt> for valid name data.
     *
     * @param data XmlTag data to check
     * @throws CFException on name mismatch
     */
    public void checkValidNameAndOwner(XmlTag data) throws CFException {
        if (data.getName() == null || data.getName().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid tag name (null or empty string)");
        }
    }

    /**
     * Check all tags in <tt>data</tt> for valid name data.
     *
     * @param data XmlTags data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlTags data) throws CFException {
        if (data == null || data.getTags() == null) return;
        for (XmlTag t : data.getTags()) {
            checkValidNameAndOwner(t);
        }
    }

    /**
     * Check that <tt>name</tt> matches the logbook name in <tt>data</tt>.
     *
     * @param name logbook name to check
     * @param data XmlLogbook data to check against
     * @throws CFException on name mismatch
     */
    public void checkNameMatchesPayload(String name, XmlLogbook data) throws CFException {
        if (!name.equals(data.getName())) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Specified logbook name '" + name
                    + "' and payload logbook name '" + data.getName() + "' do not match");
        }
    }

    /**
     * Check the logbook in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlLogbook data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlLogbook data) throws CFException {
        if (data.getName() == null || data.getName().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid logbook name (empty string)");
        }
        if (data.getOwner() == null || data.getOwner().equals("")) {
            throw new CFException(Response.Status.BAD_REQUEST,
                    "Invalid logbook owner (null or empty string) for '" + data.getName() + "'");
        }
    }

    /**
     * Check all logbooks in <tt>data</tt> for valid name/owner data.
     *
     * @param data XmlLogbooks data to check
     * @throws CFException on error
     */
    public void checkValidNameAndOwner(XmlLogbooks data) throws CFException {
        if (data == null || data.getLogbooks() == null) return;
        for (XmlLogbook p : data.getLogbooks()) {
            checkValidNameAndOwner(p);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the database for
     * log <tt>logId</tt>.
     *
     * @param user user name
     * @param chan name of log to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroupOfLog(String user, int logId) throws CFException {
        if (logId == 0) return;
        checkUserBelongsToGroup(user, FindLogsQuery.findLogById(logId));
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the database for
     * logbook <tt>logbook</tt>.
     *
     * @param user user name
     * @param logbook name of logbook to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroupOfLogbook(String user, String logbook) throws CFException {
        if (logbook == null || logbook.equals("")) return;
        checkUserBelongsToGroup(user, ListLogbooksQuery.findLogbook(logbook));
    }


    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the
     * log <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlLog data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlLog data) throws CFException {
        if (data == null) return;
        UserManager um = UserManager.getInstance();
        if (!um.userIsInGroup(data.getOwner())) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "User '" + um.getUserName()
                    + "' does not belong to owner group '" + data.getOwner()
                    + "' of log '" + data.getId() + "'");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner groups of all logs in <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlLogs data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlLogs data) throws CFException {
        if (data == null || data.getLogs() == null) return;
        for (XmlLog log : data.getLogs()) {
            checkUserBelongsToGroup(user, log);
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner group specified in the
     * logbook <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlLogbook data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlLogbook data) throws CFException {
        if (data == null) return;
        UserManager um = UserManager.getInstance();
        if (!um.userIsInGroup(data.getOwner())) {
            throw new CFException(Response.Status.FORBIDDEN,
                    "User '" + um.getUserName()
                    + "' does not belong to owner group '" + data.getOwner()
                    + "' of logbook '" + data.getName() + "'");
        }
    }

    /**
     * Check that <tt>user</tt> belongs to the owner groups of all logbooks in <tt>data</tt>.
     *
     * @param user user name
     * @param data XmlLogs data to check ownership for
     * @throws CFException on name mismatch
     */
    public void checkUserBelongsToGroup(String user, XmlLogbooks data) throws CFException {
        if (data == null || data.getLogbooks() == null) return;
        for (XmlLogbook logbook : data.getLogbooks()) {
            checkUserBelongsToGroup(user, logbook);
        }
    }
}