# 🌐 SATMOTOR Project Git Flow Strategy

The current Git Flow for the SATMOTOR project uses four main branches. These branches serve four specific, isolated environments and help to separate the code versions for each environment. This separation facilitates easier maintenance, issue fixing, provides multiple version storage environments, and offers better transparency for both the Development (Dev) Team and the IT/Deployment Team.

## 🌳 Main Branches and CI Integration

The four main branches configured on Git with Continuous Integration (CI) build are:

- **DEV** - Development Environment
- **PRE-PROD** - Pre-Production Environment (Production-like)
- **UATBAU** - UAT Business As Usual Environment
- **UATPROJ** - UAT Project Environment

## 🛠️ Feature Development and Issue Fixing Workflow

When a requirement for a new feature or a request to review and fix an issue on production arises:

1. The Dev team can directly checkout the latest code from the branch currently deployed on production (PRE-PROD).
2. The developer then creates a `feature_branch` or any other designated branch to run the code locally and implement/fix the issue.

## ✅ Development and Testing on DEV Environment

After completing the issue fix or feature development:

1. The developer merges the code into the **DEV** branch.
2. A Jenkins CI pipeline is automatically triggered, which builds and simultaneously deploys the code to the Dev environment (Dev Env).
3. The Dev Team uses the Dev Env to run function tests on the newly implemented version.
4. If an issue is found, the developer can checkout the code again, modify it, and the process repeats.

## 🔄 Workflow for Other Environments

A similar process applies to the remaining environments. For clear visibility:

- Each environment has its own dedicated Git branch with a clear naming convention.
- Any feature development can be merged into a specific environment branch with a clear objective.
- Jenkins will automatically trigger the CI process and then deploy to the correct environment corresponding to that branch's name.

## 🚀 Production Promotion Pipeline

After successful deployment and testing in the PRE-PROD environment:

1. The PRE-PROD environment undergoes comprehensive testing and validation.
2. Once all tests pass and the build is verified, a **Promotion Pipeline** is triggered.
3. The Promotion Pipeline promotes the PRE-PROD Docker image to the **PROD ECR** (Elastic Container Registry).
4. The image is now available in PROD ECR and ready for production deployment.
5. The production release can be scheduled and deployed using the promoted image from PROD ECR.

**Note:** The promotion pipeline ensures that only tested and validated images from PRE-PROD are available for production deployment, maintaining a clear separation between pre-production and production container registries.

## 🔒 Branch Protection and Approval Process (Pull Requests)

All current branches are configured with protection rules and can only be merged via a Pull Request (PR). This ensures that a Developer Lead or Manager can review the code changes and approve whether the code is suitable for merging into that branch for deployment.

---

## 📊 Visual Git Flow Diagrams

### 1. Git Graph - Branch Structure

```mermaid
---
config:
  theme: redux-color
---
gitGraph
    commit id: "Initial"
    
    branch PRE-PROD
    checkout PRE-PROD
    commit id: "Production Code"
    
    branch DEV
    checkout DEV
    commit id: "Dev Base"
    
    branch UATBAU
    checkout UATBAU
    commit id: "UATBAU Base"
    
    branch UATPROJ
    checkout UATPROJ
    commit id: "UATPROJ Base"
    
    checkout PRE-PROD
    branch feature/new-feature
    checkout feature/new-feature
    commit id: "Feature Development"
    

    checkout DEV
    merge feature/new-feature 
    commit id: "PR Approved - Deploy to DEV"
    
    checkout UATBAU
    merge DEV
    merge feature/new-feature
    commit id: "PR Approved - Deploy to UATBAU"
    
    checkout UATPROJ
    merge feature/new-feature
    commit id: "PR Approved - Deploy to UATPROJ"
    
    checkout PRE-PROD
    merge DEV
    merge UATBAU
    merge feature/new-feature
    commit id: "PR + Manager Review - Deploy to PRE-PROD"
    commit id: "PRE-PROD Build & Test Done"
    commit id: "Promotion Pipeline - Image to PROD ECR"
    commit id: "Ready for Production Deployment"
```

### 2. Detailed Workflow Diagram

```mermaid
flowchart TD
    Start([New Feature/Issue Request]) --> Checkout[Checkout from PRE-PROD<br/>Production Branch]
    Checkout --> Feature[Create Feature Branch<br/>feature/xxx]
    Feature --> Dev[Local Development & Testing]
    Dev --> MergeDev[Merge to DEV Branch]
    MergeDev --> PR1[Create Pull Request]
    PR1 --> Review1[Developer Lead Review]
    Review1 -->|Approved| CI1[Jenkins CI Pipeline]
    CI1 --> DeployDev[Deploy to DEV Environment]
    DeployDev --> TestDev[Function Testing]
    TestDev -->|Issues Found| Dev
    TestDev -->|Pass| MergeUATBAU[Merge to UATBAU Branch]
    
    MergeUATBAU --> PR2[Create Pull Request]
    PR2 --> Review2[Developer Lead Review]
    Review2 -->|Approved| CI2[Jenkins CI Pipeline]
    CI2 --> DeployUATBAU[Deploy to UATBAU Environment]
    
    TestDev -->|Pass| MergeUATPROJ[Merge to UATPROJ Branch]
    MergeUATPROJ --> PR3[Create Pull Request]
    PR3 --> Review3[Developer Lead Review]
    Review3 -->|Approved| CI3[Jenkins CI Pipeline]
    CI3 --> DeployUATPROJ[Deploy to UATPROJ Environment]
    
    DeployUATBAU --> MergePREPROD[Merge to PRE-PROD Branch]
    MergePREPROD --> PR4[Create Pull Request]
    PR4 --> Review4[Manager + Developer Lead Review]
    Review4 -->|Approved| CI4[Jenkins CI Pipeline]
    CI4 --> DeployPREPROD[Deploy to PRE-PROD Environment]
    DeployPREPROD --> TestPREPROD[PRE-PROD Testing & Validation]
    TestPREPROD -->|Pass| Promotion[Promotion Pipeline Triggered]
    Promotion --> PromoteECR[Promote Image to PROD ECR]
    PromoteECR --> ReadyProd[Ready for Production Deployment]
    ReadyProd --> End([Complete])
    TestPREPROD -->|Issues Found| Dev
    
    style Start fill:#e1f5ff
    style End fill:#c8e6c9
    style Feature fill:#fff9c4
    style PR1 fill:#ffccbc
    style PR2 fill:#ffccbc
    style PR3 fill:#ffccbc
    style PR4 fill:#ffccbc
    style Review1 fill:#b3e5fc
    style Review2 fill:#b3e5fc
    style Review3 fill:#b3e5fc
    style Review4 fill:#b3e5fc
    style DeployDev fill:#c5cae9
    style DeployUATBAU fill:#c5cae9
    style DeployUATPROJ fill:#c5cae9
    style DeployPREPROD fill:#c5cae9
    style TestPREPROD fill:#fff3e0
    style Promotion fill:#f8bbd0
    style PromoteECR fill:#e1bee7
    style ReadyProd fill:#c8e6c9
```

### 3. Branch Structure and Flow

```mermaid
graph LR
    subgraph "Main Branches"
        PREPROD[PRE-PROD<br/>Production]
        DEV[DEV<br/>Development]
        UATBAU[UATBAU<br/>UAT Business]
        UATPROJ[UATPROJ<br/>UAT Project]
    end
    
    subgraph "Feature Branch"
        FEATURE[feature/xxx<br/>Feature Branch]
    end
    
    subgraph "Container Registry"
        PRODECR[PROD ECR<br/>Production Registry]
    end
    
    PREPROD -.->|Checkout| FEATURE
    FEATURE -->|PR + Review| DEV
    DEV -->|PR + Review| UATBAU
    DEV -->|PR + Review| UATPROJ
    UATBAU -->|PR + Manager Review| PREPROD
    PREPROD -->|Promotion Pipeline| PRODECR
    
    style PREPROD fill:#1ba1e2,color:#fff
    style DEV fill:#d80073,color:#fff
    style UATBAU fill:#6a00ff,color:#fff
    style UATPROJ fill:#6a00ff,color:#fff
    style FEATURE fill:#f0a30a,color:#fff
    style PRODECR fill:#00c853,color:#fff
```

### 4. Production Promotion Pipeline Flow

```mermaid
flowchart TD
    PREPRODDeploy[PRE-PROD Deployment Complete] --> PREPRODTest[PRE-PROD Testing & Validation]
    PREPRODTest -->|All Tests Pass| TriggerPromotion[Promotion Pipeline Triggered]
    PREPRODTest -->|Issues Found| FixIssues[Fix Issues & Re-deploy]
    FixIssues --> PREPRODTest
    
    TriggerPromotion --> BuildImage[Build PRE-PROD Image]
    BuildImage --> TagImage[Tag Image with Version]
    TagImage --> PushPREPRODECR[Push to PRE-PROD ECR]
    PushPREPRODECR --> PromoteImage[Promote Image to PROD ECR]
    PromoteImage --> VerifyECR[Verify Image in PROD ECR]
    VerifyECR --> ReadyForProd[Image Ready for Production Deployment]
    ReadyForProd --> ScheduleRelease[Schedule Production Release]
    
    style PREPRODDeploy fill:#c5cae9
    style PREPRODTest fill:#fff3e0
    style TriggerPromotion fill:#f8bbd0
    style BuildImage fill:#e1bee7
    style TagImage fill:#e1bee7
    style PushPREPRODECR fill:#e1bee7
    style PromoteImage fill:#e1bee7
    style VerifyECR fill:#c8e6c9
    style ReadyForProd fill:#c8e6c9
    style ScheduleRelease fill:#c8e6c9
    style FixIssues fill:#ffccbc
```

### 5. Environment Deployment Flow

```mermaid
sequenceDiagram
    participant Dev as Developer
    participant Git as Git Repository
    participant PR as Pull Request
    participant Jenkins as Jenkins CI/CD
    participant Env as Environment
    participant PRODECR as PROD ECR
    
    Dev->>Git: Checkout from PRE-PROD
    Dev->>Git: Create feature/xxx branch
    Dev->>Git: Develop & Commit locally
    Dev->>Git: Push feature branch
    Dev->>PR: Create PR to DEV
    PR->>PR: Developer Lead Review
    PR->>Git: Merge to DEV (Approved)
    Git->>Jenkins: Trigger CI Pipeline
    Jenkins->>Env: Deploy to DEV Environment
    Env-->>Dev: Function Testing
    
    alt Testing Pass
        Dev->>PR: Create PR to UATBAU
        PR->>PR: Developer Lead Review
        PR->>Git: Merge to UATBAU
        Git->>Jenkins: Trigger CI Pipeline
        Jenkins->>Env: Deploy to UATBAU
        
        Dev->>PR: Create PR to UATPROJ
        PR->>PR: Developer Lead Review
        PR->>Git: Merge to UATPROJ
        Git->>Jenkins: Trigger CI Pipeline
        Jenkins->>Env: Deploy to UATPROJ
        
        Dev->>PR: Create PR to PRE-PROD
        PR->>PR: Manager + DL Review
        PR->>Git: Merge to PRE-PROD
        Git->>Jenkins: Trigger CI Pipeline
        Jenkins->>Env: Deploy to PRE-PROD
        Env-->>Dev: PRE-PROD Testing & Validation
        
        alt PRE-PROD Tests Pass
            Jenkins->>Jenkins: Promotion Pipeline Triggered
            Jenkins->>PRODECR: Promote Image to PROD ECR
            PRODECR-->>Dev: Image Ready for Production
        else PRE-PROD Issues Found
            Env-->>Dev: Report Issues
            Dev->>Git: Fix & Update feature branch
        end
    else Issues Found
        Env-->>Dev: Report Issues
        Dev->>Git: Fix & Update feature branch
    end
```

## 📋 Branch Protection Rules Summary

| Branch | Protection | Required Reviewers | Merge Method |
|--------|-----------|-------------------|--------------|
| **DEV** | ✅ Protected | Developer Lead | Pull Request Only |
| **UATBAU** | ✅ Protected | Developer Lead | Pull Request Only |
| **UATPROJ** | ✅ Protected | Developer Lead | Pull Request Only |
| **PRE-PROD** | ✅ Protected | Manager + Developer Lead | Pull Request Only |

## 🔄 Typical Workflow Steps

1. **Start Development**
   ```bash
   git checkout PRE-PROD
   git pull origin PRE-PROD
   git checkout -b feature/new-feature
   ```

2. **Develop Locally**
   - Make changes
   - Test locally
   - Commit changes

3. **Push and Create PR to DEV**
   ```bash
   git push origin feature/new-feature
   # Create PR via Git web interface
   ```

4. **After PR Approval**
   - Code merges to DEV
   - Jenkins automatically builds and deploys to DEV environment
   - Team tests in DEV environment

5. **Promote to Other Environments**
   - Create PR from DEV to UATBAU/UATPROJ
   - After approval, Jenkins deploys to respective environments

6. **Final Promotion to PRE-PROD**
   - Create PR from UATBAU to PRE-PROD
   - Requires Manager + Developer Lead approval
   - After approval, Jenkins deploys to PRE-PROD environment
   - Run comprehensive testing and validation in PRE-PROD

7. **Production Promotion Pipeline**
   - After PRE-PROD build and testing are completed successfully
   - Promotion Pipeline is automatically triggered
   - PRE-PROD Docker image is promoted to PROD ECR
   - Image is now available in PROD ECR and ready for production deployment
   - Production release can be scheduled using the promoted image

## 📝 Notes

- All branches are protected and require Pull Request for merging
- Each environment has its own dedicated branch
- Jenkins CI/CD automatically triggers on merge to any main branch
- Feature branches should be deleted after successful merge
- Always checkout from PRE-PROD (production branch) when starting new features
- Promotion Pipeline automatically promotes PRE-PROD images to PROD ECR after successful testing
- Only tested and validated images from PRE-PROD are available in PROD ECR for production deployment

---

## 🌐 Supported Git Platforms for Diagram Viewing

This document uses **Mermaid** diagrams which are supported natively by:

- ✅ **GitHub** - Full support, renders automatically in Markdown files
- ✅ **GitLab** - Full support, renders automatically in Markdown files
- ✅ **Bitbucket** - Supported (version 2021+)
- ✅ **Azure DevOps** - Supported with extensions
- ✅ **Gitea** - Supported (version 1.12+)

You can also preview these diagrams at: https://mermaid.live

---

*Last Updated: Nov 2025*

