package com.peertutor.dao;

import com.peertutor.util.DBConnection;

import java.sql.Connection;

/**
 * Base class providing shared access to the database connection.
 */
public abstract class BaseDAO {

    protected Connection getConnection() {
        return DBConnection.getConnection();
    }
}
