package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BackupData(
    val subjects: List<SubjectEntity>?,
    val chapters: List<ChapterEntity>?,
    val studySessions: List<StudySessionEntity>?,
    val admissionTargets: List<AdmissionTargetEntity>?,
    val plannerTasks: List<PlannerTaskEntity>?,
    val revisionItems: List<RevisionItemEntity>?,
    val mockTests: List<MockTestEntity>?,
    val mistakes: List<MistakeEntity>?,
    val focusSchedules: List<FocusScheduleEntity>?
)
