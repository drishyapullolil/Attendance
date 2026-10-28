package com.example.myapplication22;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "AttendanceDB.db";
    private static final int DATABASE_VERSION = 1;

    private static final String STUDENT_TABLE = "students";
    private static final String ATTENDANCE_TABLE = "attendance";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createStudentTable =
                "CREATE TABLE " + STUDENT_TABLE + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "roll_no TEXT UNIQUE NOT NULL, " +
                        "name TEXT NOT NULL" +
                        ")";

        db.execSQL(createStudentTable);

        String createAttendanceTable =
                "CREATE TABLE " + ATTENDANCE_TABLE + " (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "student_id INTEGER NOT NULL, " +
                        "attendance_date TEXT NOT NULL, " +
                        "status TEXT NOT NULL, " +
                        "UNIQUE(student_id, attendance_date), " +
                        "FOREIGN KEY(student_id) REFERENCES students(id) ON DELETE CASCADE" +
                        ")";

        db.execSQL(createAttendanceTable);
    }

    @Override
    public void onUpgrade(
            SQLiteDatabase db,
            int oldVersion,
            int newVersion
    ) {
        db.execSQL("DROP TABLE IF EXISTS " + ATTENDANCE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + STUDENT_TABLE);
        onCreate(db);
    }

    // Add a new student
    public boolean addStudent(String rollNo, String name) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("roll_no", rollNo);
        values.put("name", name);

        long result = db.insert(STUDENT_TABLE, null, values);

        db.close();

        return result != -1;
    }

    // Get all students
    public Cursor getAllStudents() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT id, roll_no, name FROM " + STUDENT_TABLE +
                        " ORDER BY roll_no",
                null
        );
    }

    // Mark attendance for a student
    public boolean markAttendance(
            int studentId,
            String date,
            String status
    ) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("student_id", studentId);
        values.put("attendance_date", date);
        values.put("status", status);

        long result = db.insertWithOnConflict(
                ATTENDANCE_TABLE,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );

        db.close();

        return result != -1;
    }

    // Get all attendance records
    public Cursor getAllAttendance() {

        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT students.roll_no, students.name, " +
                        "attendance.attendance_date, attendance.status " +
                        "FROM attendance " +
                        "INNER JOIN students " +
                        "ON students.id = attendance.student_id " +
                        "ORDER BY attendance.attendance_date DESC, students.roll_no";

        return db.rawQuery(query, null);
    }

    // Delete a student
    public boolean deleteStudent(int studentId) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                STUDENT_TABLE,
                "id = ?",
                new String[]{String.valueOf(studentId)}
        );

        db.close();

        return result > 0;
    }
}
