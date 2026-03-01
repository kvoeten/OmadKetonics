package com.kazvoeten.omadketonics.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE recipes
            ADD COLUMN recipe_icon TEXT NOT NULL DEFAULT '🍽️'
            """.trimIndent(),
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            ALTER TABLE recipes
            ADD COLUMN recipe_image_uri TEXT
            """.trimIndent(),
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS health_daily_summary (
                date TEXT NOT NULL PRIMARY KEY,
                sleep_total_minutes INTEGER NOT NULL,
                sleep_deep_minutes INTEGER NOT NULL,
                sleep_rem_minutes INTEGER NOT NULL,
                sleep_light_minutes INTEGER NOT NULL,
                sleep_session_count INTEGER NOT NULL,
                exercise_minutes INTEGER NOT NULL,
                active_calories INTEGER NOT NULL,
                activity_session_count INTEGER NOT NULL,
                high_intensity_sessions INTEGER NOT NULL,
                moderate_intensity_sessions INTEGER NOT NULL,
                low_intensity_sessions INTEGER NOT NULL,
                source TEXT NOT NULL,
                updated_at_epoch INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_health_daily_summary_date ON health_daily_summary(date)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS manual_activity_logs (
                id TEXT NOT NULL PRIMARY KEY,
                start_time_epoch INTEGER NOT NULL,
                end_time_epoch INTEGER NOT NULL,
                activity_type TEXT NOT NULL,
                exertion INTEGER NOT NULL,
                calories INTEGER NOT NULL,
                source TEXT NOT NULL,
                outbox_status TEXT NOT NULL,
                health_client_record_id TEXT,
                notes TEXT,
                created_at_epoch INTEGER NOT NULL,
                synced_at_epoch INTEGER
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_manual_activity_logs_start_time_epoch ON manual_activity_logs(start_time_epoch)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_manual_activity_logs_outbox_status ON manual_activity_logs(outbox_status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_manual_activity_logs_health_client_record_id ON manual_activity_logs(health_client_record_id)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS health_outbox (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                item_type TEXT NOT NULL,
                payload_json TEXT NOT NULL,
                status TEXT NOT NULL,
                attempts INTEGER NOT NULL,
                last_error TEXT,
                created_at_epoch INTEGER NOT NULL,
                updated_at_epoch INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_health_outbox_status ON health_outbox(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_health_outbox_item_type ON health_outbox(item_type)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_health_outbox_created_at_epoch ON health_outbox(created_at_epoch)")

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS health_sync_state (
                key TEXT NOT NULL PRIMARY KEY,
                value_text TEXT,
                value_long INTEGER
            )
            """.trimIndent(),
        )
    }
}

