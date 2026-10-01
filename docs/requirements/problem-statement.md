# Problem Statement – Thesis Submission Portal

## 1. Background

In many academic institutions, thesis submission and review activities are handled using email, shared drives, messaging applications, or manual record keeping.

Students submit thesis documents to faculty members, reviewers provide feedback separately, and students may submit multiple corrected versions.

This makes it difficult to maintain a centralized record of submissions, identify the latest thesis version, track review status, and maintain a history of reviewer decisions.

## 2. Problem

The absence of a centralized thesis management system can result in:

- Multiple thesis versions being exchanged through email or messaging applications.
- Difficulty identifying the latest submitted document.
- Lack of visibility into the current review status.
- Manual tracking of approval and rejection decisions.
- Difficulty maintaining reviewer comments and revision history.
- Limited traceability of actions performed by students and reviewers.
- Manual deployment and testing of application updates.

## 3. Proposed Solution

The Thesis Submission Portal will provide a centralized web-based platform through which students can submit thesis documents and reviewers can review, approve, reject, or request modifications.

The system will maintain submission versions, reviewer comments, status history and audit information.

The application will also follow a DevOps workflow using Git/GitHub, Jenkins, Selenium, Docker and Ansible to automate building, testing, deployment and infrastructure configuration.

## 4. Target Users

The primary users are:

- Students
- Faculty Guides
- Thesis Reviewers
- Administrators

## 5. Core Workflow

Student Login
      ↓
Submit Thesis
      ↓
Validate Submission
      ↓
Store Thesis
      ↓
Reviewer Dashboard
      ↓
Review Thesis
      ↓
Approve / Reject / Request Changes
      ↓
Update Status
      ↓
Student Views Result

If changes are requested:

Request Changes
      ↓
Student Uploads Revised Version
      ↓
New Version Created
      ↓
Reviewer Reviews Again

## 6. Objectives

The project aims to:

1. Provide centralized thesis submission.
2. Validate thesis information and uploaded documents.
3. Maintain multiple thesis versions.
4. Provide structured reviewer workflows.
5. Track thesis status throughout the review lifecycle.
6. Maintain reviewer comments and audit history.
7. Automate application testing using Selenium.
8. Automate build and integration using Jenkins.
9. Containerize the application using Docker.
10. Automate deployment and server configuration using Ansible.

## 7. Success Criteria

The project will be considered successful when:

- A student can submit a valid thesis.
- Invalid submissions are rejected with appropriate messages.
- Reviewers can access submitted theses.
- Reviewers can approve, reject or request changes.
- Students can view the latest status.
- Revised submissions maintain version history.
- Automated Selenium tests verify critical workflows.
- Jenkins automatically builds and tests the application.
- Failed tests prevent deployment.
- Docker can package and run the application.
- Jenkins can automatically deploy a successful Docker image.
- Ansible can configure the deployment environment.
- Application health can be automatically verified after deployment.
- A previous stable release can be restored when required.