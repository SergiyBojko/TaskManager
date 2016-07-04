package com.serhiyboiko.taskmanager.utils.realm_io;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created on 28.06.2016.
 */
public class TaskMigration implements RealmMigration {
    @Override
    public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();

        if (oldVersion == 1){

            schema.create("PauseInfo")
                    .addField("mPauseStart", long.class)
                    .addField("mPauseDuration", long.class);
            schema.create("TaskExecInfo")
                    .addField("mTaskStart", long.class)
                    .addField("mTaskEnd", long.class)
                    .addField("mDuration", long.class);
            schema.get("Task")
                    .addField("mTaskMaxDuration", int.class)
                    .addRealmListField("mPauseInfoList", schema.get("PauseInfo"))
                    .addRealmListField("mTaskExecInfoList", schema.get("TaskExecInfo"))
                    .addField("mAvatarLocation", String.class)
                    .addField("mPeriod", int.class)
                    .addField("mIsPaused", boolean.class)
                    .addField("mIsHidden", boolean.class);
            oldVersion++;
        }

    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TaskMigration;
    }
}