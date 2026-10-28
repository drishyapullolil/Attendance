package com.example.myapplication22;

import android.app.Activity;
import android.os.Bundle;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    EditText editRollNo, editName;

    Button btnAddStudent;
    Button btnPresent;
    Button btnAbsent;
    Button btnViewAttendance;
    Button btnDeleteStudent;

    ListView listStudents;
    TextView textSelectedStudent;

    com.example.myapplication22.DatabaseHelper databaseHelper;

    ArrayList<String> studentList;
    ArrayList<Integer> studentIds;
    ArrayList<String> studentNames;
    ArrayList<String> studentRollNumbers;

    ArrayAdapter<String> adapter;

    int selectedStudentId = -1;
    String selectedStudentName = "";

    boolean showingAttendance = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Connect to the database
        databaseHelper = new com.example.myapplication22.DatabaseHelper(this);

        // Enable foreign-key constraints
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.setForeignKeyConstraintsEnabled(true);
        db.close();

        // Connect Java variables to XML components
        editRollNo = findViewById(R.id.editRollNo);
        editName = findViewById(R.id.editName);

        btnAddStudent = findViewById(R.id.btnAddStudent);
        btnPresent = findViewById(R.id.btnPresent);
        btnAbsent = findViewById(R.id.btnAbsent);
        btnViewAttendance = findViewById(R.id.btnViewAttendance);
        btnDeleteStudent = findViewById(R.id.btnDeleteStudent);

        listStudents = findViewById(R.id.listStudents);
        textSelectedStudent = findViewById(R.id.textSelectedStudent);

        // Create lists
        studentList = new ArrayList<>();
        studentIds = new ArrayList<>();
        studentNames = new ArrayList<>();
        studentRollNumbers = new ArrayList<>();

        // Connect the list to the screen
        adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                studentList
        );

        listStudents.setAdapter(adapter);

        // Display all registered students
        loadStudents();

        // ADD STUDENT
        btnAddStudent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String rollNo =
                        editRollNo.getText().toString().trim();

                String name =
                        editName.getText().toString().trim();

                if (rollNo.isEmpty() || name.isEmpty()) {

                    Toast.makeText(
                            MainActivity.this,
                            "Please enter roll number and name",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }

                boolean inserted =
                        databaseHelper.addStudent(rollNo, name);

                if (inserted) {

                    Toast.makeText(
                            MainActivity.this,
                            "Student added successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    editRollNo.setText("");
                    editName.setText("");

                    selectedStudentId = -1;
                    selectedStudentName = "";

                    textSelectedStudent.setText(
                            "No student selected"
                    );

                    loadStudents();

                } else {

                    Toast.makeText(
                            MainActivity.this,
                            "Could not add student. Check if roll number already exists.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });

        // SELECT A STUDENT FROM THE LIST
        listStudents.setOnItemClickListener(
                (parent, view, position, id) -> {

                    if (showingAttendance) {

                        Toast.makeText(
                                MainActivity.this,
                                "Switch to View Students first",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    selectedStudentId =
                            studentIds.get(position);

                    selectedStudentName =
                            studentNames.get(position);

                    textSelectedStudent.setText(
                            "Selected: " + selectedStudentName
                    );

                    Toast.makeText(
                            MainActivity.this,
                            "Student selected",
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );

        // MARK PRESENT
        btnPresent.setOnClickListener(v -> {

            markAttendance("Present");
        });

        // MARK ABSENT
        btnAbsent.setOnClickListener(v -> {

            markAttendance("Absent");
        });

        // VIEW ATTENDANCE / VIEW STUDENTS
        btnViewAttendance.setOnClickListener(v -> {

            if (!showingAttendance) {

                loadAttendance();

                showingAttendance = true;

                btnViewAttendance.setText("View Students");

                textSelectedStudent.setText(
                        "Attendance Records"
                );

            } else {

                showingAttendance = false;

                btnViewAttendance.setText("View Attendance");

                selectedStudentId = -1;
                selectedStudentName = "";

                textSelectedStudent.setText(
                        "No student selected"
                );

                loadStudents();
            }
        });

        // DELETE SELECTED STUDENT
        btnDeleteStudent.setOnClickListener(v -> {

            if (showingAttendance) {

                Toast.makeText(
                        MainActivity.this,
                        "Switch to View Students first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (selectedStudentId == -1) {

                Toast.makeText(
                        MainActivity.this,
                        "Please select a student first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            boolean deleted =
                    databaseHelper.deleteStudent(
                            selectedStudentId
                    );

            if (deleted) {

                Toast.makeText(
                        MainActivity.this,
                        "Student deleted successfully",
                        Toast.LENGTH_SHORT
                ).show();

                selectedStudentId = -1;
                selectedStudentName = "";

                textSelectedStudent.setText(
                        "No student selected"
                );

                loadStudents();

            } else {

                Toast.makeText(
                        MainActivity.this,
                        "Unable to delete student",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    // LOAD STUDENTS FROM DATABASE
    private void loadStudents() {

        studentList.clear();
        studentIds.clear();
        studentNames.clear();
        studentRollNumbers.clear();

        Cursor cursor = databaseHelper.getAllStudents();

        try {

            while (cursor.moveToNext()) {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String rollNo = cursor.getString(
                        cursor.getColumnIndexOrThrow("roll_no")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                studentIds.add(id);
                studentNames.add(name);
                studentRollNumbers.add(rollNo);

                studentList.add(
                        "Roll No: " + rollNo +
                                "\nName: " + name
                );
            }

        } finally {

            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    // MARK ATTENDANCE
    private void markAttendance(String status) {

        if (showingAttendance) {

            Toast.makeText(
                    this,
                    "Switch to View Students first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (selectedStudentId == -1) {

            Toast.makeText(
                    this,
                    "Please select a student first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String date = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        boolean saved = databaseHelper.markAttendance(
                selectedStudentId,
                date,
                status
        );

        if (saved) {

            Toast.makeText(
                    this,
                    selectedStudentName + " marked " + status,
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Failed to save attendance",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // DISPLAY ATTENDANCE RECORDS
    private void loadAttendance() {

        studentList.clear();

        Cursor cursor = databaseHelper.getAllAttendance();

        try {

            while (cursor.moveToNext()) {

                String rollNo = cursor.getString(
                        cursor.getColumnIndexOrThrow("roll_no")
                );

                String name = cursor.getString(
                        cursor.getColumnIndexOrThrow("name")
                );

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                                "attendance_date"
                        )
                );

                String status = cursor.getString(
                        cursor.getColumnIndexOrThrow("status")
                );

                studentList.add(
                        "Roll No: " + rollNo +
                                "\nName: " + name +
                                "\nDate: " + date +
                                "\nStatus: " + status
                );
            }

        } finally {

            cursor.close();
        }

        adapter.notifyDataSetChanged();

        if (studentList.isEmpty()) {

            Toast.makeText(
                    this,
                    "No attendance records found",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
