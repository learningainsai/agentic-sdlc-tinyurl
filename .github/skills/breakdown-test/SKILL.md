---
name: breakdown-test
description: 'Test Planning and Quality Assurance skill that generates comprehensive test strategies, task breakdowns, and quality validation plans (ISTQB + ISO 25010). In this SDLC it runs in the inception plan node and stores its outputs under sdlc-docs/inception/plan/.'
user-invocable: true
argument-hint: 'Inception artifacts to derive a test plan from (requirements, user stories, architecture, project plan, unit registry)'
allowed-tools: read, write
---

# Test Planning & Quality Assurance Skill

## Goal

Act as a senior Quality Assurance Engineer and Test Architect with expertise in ISTQB frameworks,
ISO 25010 quality standards, and modern testing practices. Take the inception artifacts
(requirements, user stories, architecture/design, project plan, unit registry) and generate
comprehensive test planning, task breakdown, and quality assurance documentation.

> **SDLC integration**: In this repository this skill runs as part of the **inception `plan` node**
> (alongside `breakdown-plan`), so a test strategy exists before construction. Its outputs are stored
> under `sdlc-docs/inception/plan/` and are covered by the plan node's exit gate (human approval).
> The downstream construction `testing` node consumes these artifacts.

## Quality Standards Framework

### ISTQB Framework Application

- **Test Process Activities**: Planning, monitoring, analysis, design, implementation, execution, completion
- **Test Design Techniques**: Black-box, white-box, and experience-based testing approaches
- **Test Types**: Functional, non-functional, structural, and change-related testing
- **Risk-Based Testing**: Risk assessment and mitigation strategies

### ISO 25010 Quality Model

- **Quality Characteristics**: Functional suitability, performance efficiency, compatibility, usability,
  reliability, security, maintainability, portability
- **Quality Validation**: Measurement and assessment approaches for each characteristic
- **Quality Gates**: Entry and exit criteria for quality checkpoints

## Input Requirements

Before using this skill, ensure the inception artifacts exist:

1. **Requirements**: `sdlc-docs/inception/requirements/requirements.md` (REQ-ids + acceptance criteria)
2. **User Stories**: `sdlc-docs/inception/requirements/user-stories.md` (US-ids)
3. **Architecture/Design**: `sdlc-docs/inception/architecture-design/architecture-design.md` (ADR-ids, NFRs)
4. **Project Plan**: `sdlc-docs/inception/plan/project-plan.md` (Epic/Feature/Story/Enabler/Test hierarchy)
5. **Unit Registry**: `sdlc-docs/construction/units/unit-registry.yaml` (UNIT-ids, TEST-ids)

## Output Format

Create comprehensive test planning documentation under `sdlc-docs/inception/plan/`:

1. **Test Strategy**: `sdlc-docs/inception/plan/test-strategy.md`
2. **Test Issues Checklist**: `sdlc-docs/inception/plan/test-issues-checklist.md`
3. **Quality Assurance Plan**: `sdlc-docs/inception/plan/qa-plan.md`

Every artifact must declare `template_id: test-strategy-template.md` (or the applicable template) and
trace each test item to REQ-/US-/ADR-/UNIT-/TEST- ids.

### Test Strategy Structure

#### 1. Test Strategy Overview

- **Testing Scope**: Features and components to be tested
- **Quality Objectives**: Measurable quality goals and success criteria
- **Risk Assessment**: Identified risks and mitigation strategies (link to architecture RISK-ids)
- **Test Approach**: Overall testing methodology and framework application

#### 2. ISTQB Framework Implementation

##### Test Design Techniques Selection

- **Equivalence Partitioning**: Input domain partitioning strategy
- **Boundary Value Analysis**: Edge case identification and testing
- **Decision Table Testing**: Complex business rule validation
- **State Transition Testing**: System state behavior validation
- **Experience-Based Testing**: Exploratory and error guessing approaches

##### Test Types Coverage Matrix

- **Functional Testing**: Feature behavior validation
- **Non-Functional Testing**: Performance, usability, security validation
- **Structural Testing**: Code coverage and architecture validation
- **Change-Related Testing**: Regression and confirmation testing

#### 3. ISO 25010 Quality Characteristics Assessment

Prioritize each characteristic (Critical/High/Medium/Low) with its validation approach:
Functional Suitability, Performance Efficiency, Compatibility, Usability, Reliability, Security,
Maintainability, Portability.

#### 4. Test Environment and Data Strategy

- **Test Environment Requirements**: Hardware, software, and network configurations
- **Test Data Management**: Data preparation, privacy, and maintenance strategies
- **Tool Selection**: Testing tools, frameworks, and automation platforms
- **CI/CD Integration**: Continuous testing pipeline integration

### Test Issues Checklist

#### Test Level Issues Creation

- [ ] **Test Strategy Issue**: Overall testing approach and quality validation plan
- [ ] **Unit Test Issues**: Component-level testing for each implementation task
- [ ] **Integration Test Issues**: Interface and interaction testing between components
- [ ] **End-to-End Test Issues**: Complete user workflow validation
- [ ] **Performance Test Issues**: Non-functional requirement validation
- [ ] **Security Test Issues**: Security requirement and vulnerability testing
- [ ] **Accessibility Test Issues**: WCAG compliance and inclusive design validation
- [ ] **Regression Test Issues**: Change impact and existing functionality preservation

#### Test Coverage Targets and Metrics

- [ ] **Code Coverage Targets**: >80% line coverage, >90% branch coverage for critical paths
- [ ] **Functional Coverage Targets**: 100% acceptance criteria validation
- [ ] **Risk Coverage Targets**: 100% high-risk scenario validation
- [ ] **Quality Characteristics Coverage**: Validation approach for each applicable ISO 25010 characteristic

### Task Level Breakdown

#### Task Estimation Guidelines

- [ ] **Unit Test Tasks**: 0.5-1 story point per component
- [ ] **Integration Test Tasks**: 1-2 story points per interface
- [ ] **E2E Test Tasks**: 2-3 story points per user workflow
- [ ] **Performance Test Tasks**: 3-5 story points per performance requirement
- [ ] **Security Test Tasks**: 2-4 story points per security requirement

#### Task Dependencies and Sequencing

- [ ] **Sequential Dependencies**: Tests that must be implemented in a specific order
- [ ] **Parallel Development**: Tests that can be developed simultaneously
- [ ] **Critical Path Identification**: Testing tasks on the critical path to delivery
- [ ] **Resource Allocation**: Task assignment based on team skills and capacity

### Quality Assurance Plan

#### Quality Gates and Checkpoints

- **Entry Criteria**: Requirements for beginning each testing phase
- **Exit Criteria**: Quality standards required for phase completion
- **Quality Metrics**: Measurable indicators of quality achievement
- **Escalation Procedures**: Process for addressing quality failures

#### Labeling and Prioritization Standards

- [ ] **Test Type Labels**: `unit-test`, `integration-test`, `e2e-test`, `performance-test`, `security-test`
- [ ] **Quality Labels**: `quality-gate`, `iso25010`, `istqb-technique`, `risk-based`
- [ ] **Priority Labels**: `test-critical`, `test-high`, `test-medium`, `test-low`
- [ ] **Component Labels**: `frontend-test`, `backend-test`, `api-test`, `database-test`

## Success Metrics

- **Code Coverage**: >80% line coverage, >90% branch coverage for critical paths
- **Functional Coverage**: 100% acceptance criteria validation
- **Risk Coverage**: 100% high-risk scenario testing
- **Defect Detection Rate**: >95% of defects found before production
- **Quality Gate Compliance**: 100% quality gates passed before release

This test planning approach ensures thorough quality validation aligned with ISTQB and ISO 25010,
traced end-to-end to the inception artifacts and consumed by the construction `testing` node.
