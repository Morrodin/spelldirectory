package com.zzoranor.spelldirectory.services;

import android.app.Activity;
import com.zzoranor.spelldirectory.database.DbAdapter;
import com.zzoranor.spelldirectory.database.DbAdapterFactory;

/**
 * Kind of like the secret service, but not really. Handles database connections on an abstracted level.
 *
 * @author morrodin
 */
public class SqlService {

    private Activity mActivity;
    private DbAdapter mSql;

    /**
     * Constructor
     *
     * @param activity
     *          Activity context to be setting things on
     */
    public SqlService(Activity activity) {
        this.mActivity = activity;
    }

    /**
     * Sets up the DbAdapter, including opening it.
     */
    public void setupSql() {
        mSql = DbAdapterFactory.getStaticInstance(mActivity);
        openDB();
    }

    /**
     * Opens up a connection to the database through the DbAdapter. In most cases you should use setupSql, instead of
     * this.
     */
    public void openDB() {
        if (mSql != null) {
            mSql.open();
        }
    }

    public DbAdapter getSqlAdapter() {
        return mSql;
    }

    public void setSqlAdapter(DbAdapter mSql) {
        this.mSql = mSql;
    }
}
