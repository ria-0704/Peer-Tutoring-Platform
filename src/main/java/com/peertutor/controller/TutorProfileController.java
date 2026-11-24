package com.peertutor.controller;

import com.peertutor.exception.ValidationException;
import com.peertutor.model.Skill;
import com.peertutor.model.Subject;
import com.peertutor.service.TutorService;
import com.peertutor.util.AlertUtil;
import com.peertutor.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.util.List;

/**
 * Tutor Profile page — manage subjects and descriptions.
 */
public class TutorProfileController {

    private final TutorService tutorService = new TutorService();
    private final int userId = SessionManager.getCurrentUser().getUserId();

    private VBox root;
    private TableView<Skill> skillTable;
    private ComboBox<Subject> subjectCombo;
    private TextArea descArea;

    public TutorProfileController() {
        buildUI();
    }

    private void buildUI() {
        root = new VBox(20);
        root.setPadding(new Insets(32, 36, 32, 36));
        root.setStyle("-fx-background-color: #f5f0ff;");

        Label title = new Label("My Tutor Profile");
        title.getStyleClass().add("page-title");
        Label sub = new Label("Add subjects you can teach and describe your expertise.");
        sub.getStyleClass().add("subtitle-text");

        // ── Add Skill Form ────────────────────────────────────────────────
        VBox formCard = new VBox(12);
        formCard.getStyleClass().add("card");
        formCard.setMaxWidth(600);

        Label formTitle = new Label("Add a Subject to Your Profile");
        formTitle.getStyleClass().add("section-title");

        Label subjectLbl = new Label("Subject");
        subjectLbl.getStyleClass().add("field-label");
        subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("Select a subject");
        subjectCombo.setPrefWidth(360);
        refreshSubjects();

        // Add new subject inline
        HBox newSubRow = new HBox(8);
        newSubRow.setAlignment(Pos.CENTER_LEFT);
        TextField newSubField = new TextField();
        newSubField.setPromptText("Or add a new subject name...");
        newSubField.setPrefWidth(240);
        Button addSubBtn = new Button("Add Subject");
        addSubBtn.getStyleClass().add("btn-secondary");
        newSubRow.getChildren().addAll(newSubField, addSubBtn);

        Label descLbl = new Label("Teaching Description (optional)");
        descLbl.getStyleClass().add("field-label");
        descArea = new TextArea();
        descArea.setPromptText("Describe your teaching style, experience...");
        descArea.setPrefRowCount(3);
        descArea.setPrefWidth(360);

        Button addSkillBtn = new Button("Add to Profile");
        addSkillBtn.getStyleClass().add("btn-primary");

        formCard.getChildren().addAll(
            formTitle, subjectLbl, subjectCombo, newSubRow, descLbl, descArea, addSkillBtn
        );

        // ── Skills Table ──────────────────────────────────────────────────
        Label tableTitle = new Label("Your Teaching Subjects");
        tableTitle.getStyleClass().add("section-title");

        skillTable = new TableView<>();
        skillTable.setPlaceholder(new Label("No subjects added yet. Add one above!"));
        skillTable.setPrefHeight(300);

        TableColumn<Skill, String> subjectCol = new TableColumn<>("Subject");
        subjectCol.setCellValueFactory(new PropertyValueFactory<>("subjectName"));
        subjectCol.setPrefWidth(160);

        TableColumn<Skill, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(340);

        TableColumn<Skill, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(col -> new TableCell<>() {
            final Button editBtn   = new Button("Edit");
            final Button removeBtn = new Button("Remove");
            {
                editBtn.getStyleClass().add("btn-info");
                removeBtn.getStyleClass().add("btn-danger");
                editBtn.setOnAction(e -> {
                    Skill sk = getTableView().getItems().get(getIndex());
                    handleEditSkill(sk);
                });
                removeBtn.setOnAction(e -> {
                    Skill sk = getTableView().getItems().get(getIndex());
                    handleRemoveSkill(sk);
                });
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(6, editBtn, removeBtn);
                setGraphic(box);
            }
        });

        skillTable.getColumns().addAll(subjectCol, descCol, actionCol);
        refreshSkillTable();

        // ── Events ─────────────────────────────────────────────────────
        addSubBtn.setOnAction(e -> handleAddSubject(newSubField));
        addSkillBtn.setOnAction(e -> handleAddSkill());

        root.getChildren().addAll(title, sub, formCard, tableTitle, skillTable);
    }

    private void handleAddSubject(TextField newSubField) {
        String name = newSubField.getText().trim();
        if (name.isEmpty()) {
            AlertUtil.showWarning("Input Required", "Please enter a subject name.");
            return;
        }
        try {
            tutorService.addSubject(name);
            AlertUtil.showInfo("Subject Added", "'" + name + "' added to subjects list.");
            newSubField.clear();
            refreshSubjects();
        } catch (ValidationException ex) {
            AlertUtil.showError("Error", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    private void handleAddSkill() {
        Subject selected = subjectCombo.getValue();
        if (selected == null) {
            AlertUtil.showWarning("No Subject", "Please select a subject.");
            return;
        }
        try {
            tutorService.addSkillToProfile(userId, selected.getSubjectId(), descArea.getText());
            AlertUtil.showInfo("Profile Updated", "Subject added to your profile.");
            descArea.clear();
            subjectCombo.setValue(null);
            refreshSkillTable();
        } catch (ValidationException ex) {
            AlertUtil.showError("Error", ex.getMessage());
        } catch (Exception ex) {
            AlertUtil.showError("Error", ex.getMessage());
        }
    }

    private void handleEditSkill(Skill skill) {
        TextInputDialog dialog = new TextInputDialog(skill.getDescription());
        dialog.setTitle("Edit Description");
        dialog.setHeaderText("Update your description for " + skill.getSubjectName());
        dialog.setContentText("Description:");
        dialog.showAndWait().ifPresent(newDesc -> {
            try {
                tutorService.updateSkill(skill.getSkillId(), newDesc);
                refreshSkillTable();
            } catch (ValidationException ex) {
                AlertUtil.showError("Error", ex.getMessage());
            }
        });
    }

    private void handleRemoveSkill(Skill skill) {
        if (AlertUtil.showConfirm("Remove Subject",
            "Remove '" + skill.getSubjectName() + "' from your profile?")) {
            tutorService.removeSkill(skill.getSkillId());
            refreshSkillTable();
        }
    }

    private void refreshSubjects() {
        List<Subject> subjects = tutorService.getAllSubjects();
        subjectCombo.setItems(FXCollections.observableArrayList(subjects));
    }

    private void refreshSkillTable() {
        List<Skill> skills = tutorService.getTutorSkills(userId);
        skillTable.setItems(FXCollections.observableArrayList(skills));
    }

    public VBox getView() { return root; }
}
