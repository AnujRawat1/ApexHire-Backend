# Resume Analysis System Documentation

## Overview
This document provides a comprehensive analysis of the Resume Analysis system in ApexHire, including pages, components, data structures, and required backend API endpoints.

**Architecture:** Frontend (React) → Spring Boot Backend → Python LangGraph Backend → AI Services

---

## Pages & Routes

### 1. Resume Analyzer Page (`/resume-analyzer`)
**File:** `src/routes/resume-analyzer.tsx`

**Purpose:** Main entry point for uploading and analyzing resumes

**Features:**
- PDF file upload with drag-and-drop support
- Target role selection (Frontend, Backend, Fullstack, Mobile, ML/AI, DevOps, Data, Embedded)
- Experience level selection (Junior, Mid-level, Senior, Staff+)
- Optional resume title customization
- Optional job description input for job matching
- AI-powered analysis with animated progress steps
- Recent reports display (last 5)
- File validation (PDF only, max 10MB)

**User Flow:**
1. User uploads PDF resume
2. System extracts text from PDF
3. User selects target role and experience level
4. Optionally provides job description
5. System runs AI analysis
6. Results saved and user redirected to report page

---

### 2. Resume Report Page (`/resume-report/$id`)
**File:** `src/routes/resume-report.$id.tsx`

**Purpose:** Display detailed analysis results for a specific resume

**Features:**
- Side-by-side layout: PDF viewer + Analysis panel
- Report metadata display (title, role, level, date)
- Download original resume
- Delete report functionality
- Navigation back to all reports
- Loading and error states

**Layout:**
- Left sidebar: PDF viewer with file info
- Right panel: Detailed analysis results

---

### 3. All Reports Page (`/resume-reports`)
**File:** `src/routes/resume-reports.tsx`

**Purpose:** Browse and manage all resume analysis reports

**Features:**
- Search by title, filename, or target role
- Filter by target role
- Filter by experience level
- Sort options: newest, oldest, highest score, lowest score
- Pagination (10 items per page)
- Report count display
- Click to view individual report

**Filtering & Sorting:**
- Search query filters across title, filename, and role
- Role filter: specific role or "all"
- Level filter: specific level or "all"
- Sort: date-based or score-based

---

## Components

### 1. AnalysisPanel Component
**File:** `src/components/resume/analysis-panel.tsx`

**Purpose:** Display comprehensive analysis results

**Sections Displayed:**
- **Score Overview:** Overall score, ATS score, Job match score (with visual rings)
- **Summary:** AI-generated summary of the analysis
- **Section Breakdown:** Individual scores for skills, keywords, experience, education, projects, content, formatting
- **Strengths:** List of resume strengths with checkmarks
- **Weaknesses:** List of resume weaknesses with warnings
- **Missing Skills:** Chips showing missing skills (red/danger tone)
- **Missing Keywords:** Chips showing missing keywords (muted tone)
- **AI Recommendations:** Prioritized recommendations (high/medium/low)
- **Suggested Improvements:** Numbered list of actionable improvements

---

### 2. ReportRow Component
**File:** `src/components/resume/report-row.tsx`

**Purpose:** Display individual report card in the reports list

**Display Elements:**
- Report title with navigation arrow
- Target role badge
- Experience level badge
- Date and time stamp
- Three score metrics: ATS, Job match, Overall
- Hover effects and animations

**Score Color Coding:**
- ≥80: Green (success)
- 60-79: Primary color
- <60: Destructive (red)

---

### 3. PdfViewer Component
**File:** `src/components/resume/pdf-viewer.tsx`

**Purpose:** In-app PDF viewing with custom controls

**Features:**
- Page navigation (previous/next)
- Zoom controls (in/out with percentage display)
- Fullscreen toggle
- Fallback to plain text if PDF rendering fails
- Responsive design
- Custom toolbar (no browser native controls)
- Loading states

**Technical Details:**
- Uses pdf.js library
- Canvas-based rendering
- Polyfills for Map.prototype methods
- Supports both blob and URL sources

---

## Data Structures

### 1. ResumeReport
**File:** `src/lib/resume-reports.ts`

```typescript
export type ResumeReport = {
  id: string;                    // Unique identifier
  title: string;                // User-defined or filename-based title
  fileName: string;             // Original filename
  fileSize: number;             // File size in bytes
  targetRole: TargetRole;       // Target job role
  experienceLevel: ExperienceLevel; // Experience level
  jobDescription: string | null; // Optional job description
  resumeText: string;           // Extracted text from PDF
  analysis: AnalysisResult;     // AI analysis results
  createdAt: string;            // ISO timestamp
  analyzedAt: string;           // ISO timestamp
};
```

---

### 2. AnalysisResult
**File:** `src/lib/resume-reports.ts`

```typescript
export type AnalysisResult = {
  overallScore: number;         // 0-100 overall score
  atsScore: number;             // 0-100 ATS compatibility score
  jobMatchScore: number | null; // 0-100 job match (null if no JD)
  summary: string;              // AI-generated summary
  sections: AnalysisSection[];  // Detailed section analysis
  strengths: string[];          // List of strengths
  weaknesses: string[];         // List of weaknesses
  missingSkills: string[];      // Missing skills identified
  missingKeywords: string[];   // Missing keywords identified
  recommendations: Recommendation[]; // Prioritized recommendations
  improvements: string[];       // Suggested improvements
};
```

---

### 3. AnalysisSection
**File:** `src/lib/resume-reports.ts`

```typescript
export type AnalysisSection = {
  key: string;                 // Section identifier (skills, keywords, etc.)
  title: string;               // Display title
  score: number;               // 0-100 score for this section
  summary: string;             // Section summary
  points: string[];            // Detailed points about this section
};
```

**Section Keys:**
- `skills` - Skills analysis
- `keywords` - Keywords analysis
- `experience` - Work experience analysis
- `education` - Education analysis
- `projects` - Projects analysis
- `content` - Content quality analysis
- `formatting` - Formatting analysis

---

### 4. Recommendation
**File:** `src/lib/resume-reports.ts`

```typescript
export type Recommendation = {
  title: string;               // Recommendation title
  detail: string;              // Detailed explanation
  priority: "high" | "medium" | "low"; // Priority level
};
```

---

### 5. TargetRole
**File:** `src/lib/resume-reports.ts`

```typescript
export const TARGET_ROLES = [
  "Frontend Engineer",
  "Backend Engineer",
  "Fullstack Engineer",
  "Mobile Engineer",
  "ML / AI Engineer",
  "Infrastructure / DevOps Engineer",
  "Data Engineer",
  "Embedded / Systems Engineer",
] as const;

export type TargetRole = (typeof TARGET_ROLES)[number];
```

---

### 6. ExperienceLevel
**File:** `src/lib/resume-reports.ts`

```typescript
export const EXPERIENCE_LEVELS = [
  "Junior (0-2 years)",
  "Mid-level (2-5 years)",
  "Senior (5-10 years)",
  "Staff+ (10+ years)",
] as const;

export type ExperienceLevel = (typeof EXPERIENCE_LEVELS)[number];
```

---

### 7. AnalyzeInput
**File:** `src/lib/resume-analysis.schema.ts`

```typescript
export const AnalyzeInput = z.object({
  resumeText: z.string().min(30, "Resume text is too short to analyze"),
  targetRole: z.string().min(2).max(80),
  experienceLevel: z.string().min(2).max(60),
  resumeTitle: z.string().max(120).optional(),
  jobDescription: z.string().max(20000).optional(),
});

export type AnalyzeInputType = z.infer<typeof AnalyzeInput>;
```

---

## Current Implementation Details

### Storage Architecture

**Report Metadata (LocalStorage):**
- Key format: `apex-resume-reports:{username}`
- Stores up to 100 most recent reports
- JSON serialized ResumeReport objects
- Sorted by analyzedAt date (newest first)

**PDF Files (IndexedDB):**
- Database name: `apexhire`
- Store name: `resumes`
- Key: report ID
- Value: PDF file as ArrayBuffer/Blob
- Fallback to text display if storage fails

### AI Analysis Integration

**Current Provider:** Lovable AI Gateway
**Endpoint:** `https://ai.gateway.lovable.dev/v1/chat/completions`
**Model:** `google/gemini-3.6-flash`
**Authentication:** API Key via environment variable `LOVABLE_API_KEY`

**Analysis Process:**
1. Extract text from uploaded PDF
2. Build prompt with target role, experience level, and optional job description
3. Send to AI with structured JSON response requirement
4. Parse and validate response against schema
5. Return structured analysis results

**Error Handling:**
- 429: Rate limit - "Too many requests"
- 402: Credits exhausted - "AI credits exhausted"
- Other errors: Generic failure message

---

## Required Backend API Endpoints

### Authentication
All endpoints (except where noted) require Bearer token authentication.

### 1. Analyze Resume
**Endpoint:** `POST /api/resume/analyze`

**Request Body:**
```json
{
  "resumeText": "string (min 30 chars)",
  "targetRole": "string (2-80 chars)",
  "experienceLevel": "string (2-60 chars)",
  "resumeTitle": "string (optional, max 120 chars)",
  "jobDescription": "string (optional, max 20000 chars)"
}
```

**Response:** `AnalysisResult`
```json
{
  "overallScore": 85,
  "atsScore": 78,
  "jobMatchScore": 90,
  "summary": "string",
  "sections": [...],
  "strengths": [...],
  "weaknesses [...],
  "missingSkills": [...],
  "missingKeywords": [...],
  "recommendations": [...],
  "improvements": [...]
}
```

**Purpose:** Submit resume text for AI analysis

---

### 2. Upload Resume File
**Endpoint:** `POST /api/resume/upload`

**Request:** `multipart/form-data`
- `file`: PDF file (max 10MB)

**Response:**
```json
{
  "fileId": "string",
  "fileName": "string",
  "fileSize": number,
  "uploadedAt": "ISO timestamp"
}
```

**Purpose:** Upload PDF file for storage and later retrieval

---

### 3. Get All Reports
**Endpoint:** `GET /api/resume/reports`

**Query Parameters:**
- `page`: number (default: 1)
- `limit`: number (default: 10)
- `role`: string (optional, filter by target role)
- `level`: string (optional, filter by experience level)
- `search`: string (optional, search in title/filename/role)
- `sort`: "newest" | "oldest" | "score-high" | "score-low" (default: "newest")

**Response:**
```json
{
  "reports": ResumeReport[],
  "total": number,
  "page": number,
  "limit": number,
  "totalPages": number
}
```

**Purpose:** Retrieve paginated list of user's resume reports

---

### 4. Get Specific Report
**Endpoint:** `GET /api/resume/reports/:id`

**Response:** `ResumeReport`

**Purpose:** Retrieve detailed report by ID

---

### 5. Download Resume File
**Endpoint:** `GET /api/resume/download/:id`

**Response:** PDF file (application/pdf)

**Purpose:** Download original uploaded resume file

---

### 6. Delete Report
**Endpoint:** `DELETE /api/resume/reports/:id`

**Response:**
```json
{
  "success": true,
  "message": "Report deleted successfully"
}
```

**Purpose:** Delete a report and its associated file

---

### 7. Update Report Metadata
**Endpoint:** `PUT /api/resume/reports/:id`

**Request Body:**
```json
{
  "title": "string (optional)"
}
```

**Response:** Updated `ResumeReport`

**Purpose:** Update report title or other metadata

---

## Spring Boot Backend Specifications

### Architecture Overview
The Spring Boot backend acts as an intermediary between the React frontend and Python LangGraph backend, handling:
- Authentication and authorization
- File storage and retrieval
- Database operations
- API gateway to LangGraph services
- Request validation and error handling

### Technology Stack
- **Framework:** Spring Boot 3.x
- **Java Version:** Java 17 or 21
- **Build Tool:** Maven or Gradle
- **Database:** PostgreSQL or MySQL
- **File Storage:** Local filesystem or AWS S3
- **Security:** Spring Security with JWT
- **LangGraph Integration:** WebClient or RestTemplate

### Project Structure
```
com.apexhire.resume
├── config/
│   ├── SecurityConfig.java
│   ├── WebClientConfig.java
│   ├── FileStorageConfig.java
│   └── DatabaseConfig.java
├── controller/
│   ├── ResumeController.java
│   └── FileController.java
├── service/
│   ├── ResumeService.java
│   ├── FileStorageService.java
│   ├── LangGraphService.java
│   └── AnalysisService.java
├── repository/
│   ├── ResumeReportRepository.java
│   └── UserRepository.java
├── dto/
│   ├── request/
│   │   ├── AnalyzeResumeRequest.java
│   │   └── UpdateReportRequest.java
│   └── response/
│   │       ├── AnalysisResultResponse.java
│   │       ├── ResumeReportResponse.java
│   │       └── FileUploadResponse.java
├── entity/
│   ├── ResumeReport.java
│   └── User.java
├── exception/
│   ├── ResumeAnalysisException.java
│   └── GlobalExceptionHandler.java
└── model/
    ├── langgraph/
    │   ├── LangGraphRequest.java
    │   └── LangGraphResponse.java
    └── enums/
        ├── TargetRole.java
        └── ExperienceLevel.java
```

### Database Schema

#### ResumeReport Entity
```java
@Entity
@Table(name = "resume_reports")
public class ResumeReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TargetRole targetRole;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExperienceLevel experienceLevel;
    
    @Column(columnDefinition = "TEXT")
    private String jobDescription;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String resumeText;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String analysisJson; // Stores AnalysisResult as JSON
    
    @Column(name = "file_storage_path")
    private String fileStoragePath;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "analyzed_at", nullable = false)
    private LocalDateTime analyzedAt;
    
    // Getters, setters, constructors
}
```

#### User Entity (extend existing)
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String name;
    private String email;
    private String password;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeReport> resumeReports = new ArrayList<>();
    
    // Existing fields and methods
}
```

### Spring Boot Controller Implementation

#### ResumeController
```java
@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {
    
    private final ResumeService resumeService;
    private final FileStorageService fileStorageService;
    private final AnalysisService analysisService;
    
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResultResponse> analyzeResume(
            @Valid @RequestBody AnalyzeResumeRequest request,
            @AuthenticationPrincipal User user) {
        AnalysisResultResponse result = analysisService.analyzeResume(request, user);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) {
        FileUploadResponse response = fileStorageService.uploadFile(file, user);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/reports")
    public ResponseEntity<Page<ResumeReportResponse>> getAllReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "newest") String sort,
            @AuthenticationPrincipal User user) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<ResumeReportResponse> reports = resumeService.getUserReports(
            user, pageable, role, level, search, sort);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/reports/{id}")
    public ResponseEntity<ResumeReportResponse> getReport(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        ResumeReportResponse report = resumeService.getReportById(id, user);
        return ResponseEntity.ok(report);
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        Resource file = fileStorageService.downloadFile(id, user);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
    
    @DeleteMapping("/reports/{id}")
    public ResponseEntity<DeleteResponse> deleteReport(
            @PathVariable String id,
            @AuthenticationPrincipal User user) {
        resumeService.deleteReport(id, user);
        return ResponseEntity.ok(new DeleteResponse(true, "Report deleted successfully"));
    }
    
    @PutMapping("/reports/{id}")
    public ResponseEntity<ResumeReportResponse> updateReport(
            @PathVariable String id,
            @Valid @RequestBody UpdateReportRequest request,
            @AuthenticationPrincipal User user) {
        ResumeReportResponse report = resumeService.updateReport(id, request, user);
        return ResponseEntity.ok(report);
    }
}
```

### DTO Classes

#### Request DTOs
```java
public class AnalyzeResumeRequest {
    @NotBlank(message = "Resume text is required")
    @Size(min = 30, message = "Resume text is too short to analyze")
    private String resumeText;
    
    @NotBlank(message = "Target role is required")
    @Size(min = 2, max = 80)
    private String targetRole;
    
    @NotBlank(message = "Experience level is required")
    @Size(min = 2, max = 60)
    private String experienceLevel;
    
    @Size(max = 120)
    private String resumeTitle;
    
    @Size(max = 20000)
    private String jobDescription;
    
    // Getters and setters
}

public class UpdateReportRequest {
    @Size(max = 120)
    private String title;
    
    // Getters and setters
}
```

#### Response DTOs
```java
public class AnalysisResultResponse {
    private int overallScore;
    private int atsScore;
    private Integer jobMatchScore;
    private String summary;
    private List<AnalysisSectionResponse> sections;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> missingSkills;
    private List<String> missingKeywords;
    private List<RecommendationResponse> recommendations;
    private List<String> improvements;
    
    // Getters and setters
}

public class ResumeReportResponse {
    private String id;
    private String title;
    private String fileName;
    private Long fileSize;
    private String targetRole;
    private String experienceLevel;
    private String jobDescription;
    private AnalysisResultResponse analysis;
    private String createdAt;
    private String analyzedAt;
    
    // Getters and setters
}

public class FileUploadResponse {
    private String fileId;
    private String fileName;
    private Long fileSize;
    private String uploadedAt;
    
    // Getters and setters
}
```

### Service Layer Implementation

#### LangGraphService
```java
@Service
@RequiredArgsConstructor
public class LangGraphService {
    
    private final WebClient webClient;
    
    @Value("${langgraph.api.url}")
    private String langGraphApiUrl;
    
    @Value("${langgraph.api.key}")
    private String langGraphApiKey;
    
    public AnalysisResultResponse analyzeResume(LangGraphRequest request) {
        try {
            return webClient.post()
                    .uri(langGraphApiUrl + "/analyze")
                    .header("Authorization", "Bearer " + langGraphApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(AnalysisResultResponse.class)
                    .timeout(Duration.ofMinutes(2))
                    .block();
        } catch (WebClientResponseException e) {
            throw new ResumeAnalysisException("LangGraph API error: " + e.getMessage());
        } catch (Exception e) {
            throw new ResumeAnalysisException("Analysis failed: " + e.getMessage());
        }
    }
}
```

#### AnalysisService
```java
@Service
@RequiredArgsConstructor
public class AnalysisService {
    
    private final LangGraphService langGraphService;
    private final ResumeReportRepository resumeReportRepository;
    private final FileStorageService fileStorageService;
    
    @Transactional
    public AnalysisResultResponse analyzeResume(AnalyzeResumeRequest request, User user) {
        // Build LangGraph request
        LangGraphRequest langRequest = new LangGraphRequest();
        langRequest.setResumeText(request.getResumeText());
        langRequest.setTargetRole(request.getTargetRole());
        langRequest.setExperienceLevel(request.getExperienceLevel());
        langRequest.setJobDescription(request.getJobDescription());
        
        // Call LangGraph backend
        AnalysisResultResponse analysis = langGraphService.analyzeResume(langRequest);
        
        // Save report to database
        ResumeReport report = new ResumeReport();
        report.setId(UUID.randomUUID().toString());
        report.setTitle(request.getResumeTitle() != null ? 
            request.getResumeTitle() : "Untitled Resume");
        report.setTargetRole(TargetRole.valueOf(request.getTargetRole()));
        report.setExperienceLevel(ExperienceLevel.valueOf(request.getExperienceLevel()));
        report.setJobDescription(request.getJobDescription());
        report.setResumeText(request.getResumeText());
        report.setAnalysisJson(convertToJson(analysis));
        report.setUser(user);
        report.setCreatedAt(LocalDateTime.now());
        report.setAnalyzedAt(LocalDateTime.now());
        
        resumeReportRepository.save(report);
        
        return analysis;
    }
    
    private String convertToJson(AnalysisResultResponse analysis) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(analysis);
        } catch (JsonProcessingException e) {
            throw new ResumeAnalysisException("Failed to serialize analysis result");
        }
    }
}
```

#### FileStorageService
```java
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    @Value("${file.storage.location}")
    private String storageLocation;
    
    @Transactional
    public FileUploadResponse uploadFile(MultipartFile file, User user) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 10MB limit");
        }
        
        // Generate unique filename
        String fileId = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".pdf";
        String storedFilename = fileId + fileExtension;
        
        // Save file
        Path storagePath = Paths.get(storageLocation, user.getId(), "resumes");
        try {
            Files.createDirectories(storagePath);
            Path filePath = storagePath.resolve(storedFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            FileUploadResponse response = new FileUploadResponse();
            response.setFileId(fileId);
            response.setFileName(originalFilename);
            response.setFileSize(file.getSize());
            response.setUploadedAt(LocalDateTime.now().toString());
            
            return response;
        } catch (IOException e) {
            throw new ResumeAnalysisException("Failed to store file: " + e.getMessage());
        }
    }
    
    public Resource downloadFile(String reportId, User user) {
        ResumeReport report = resumeReportRepository.findByIdAndUser(reportId, user)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (report.getFileStoragePath() == null) {
            throw new RuntimeException("File not available for this report");
        }
        
        try {
            Path filePath = Paths.get(report.getFileStoragePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("File not found or not readable");
            }
            
            return resource;
        } catch (MalformedURLException e) {
            throw new RuntimeException("File access error", e);
        }
    }
    
    @Transactional
    public void deleteFile(String reportId, User user) {
        ResumeReport report = resumeReportRepository.findByIdAndUser(reportId, user)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (report.getFileStoragePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(report.getFileStoragePath()));
            } catch (IOException e) {
                // Log error but continue with database deletion
            }
        }
    }
}
```

#### ResumeService
```java
@Service
@RequiredArgsConstructor
public class ResumeService {
    
    private final ResumeReportRepository resumeReportRepository;
    private final FileStorageService fileStorageService;
    
    public Page<ResumeReportResponse> getUserReports(
            User user, Pageable pageable, String role, String level, 
            String search, String sort) {
        
        Specification<ResumeReport> spec = Specification.where(null);
        
        // User filter
        spec = spec.and((root, query, cb) -> 
            cb.equal(root.get("user"), user));
        
        // Role filter
        if (role != null && !role.equals("all")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("targetRole"), TargetRole.valueOf(role)));
        }
        
        // Level filter
        if (level != null && !level.equals("all")) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("experienceLevel"), ExperienceLevel.valueOf(level)));
        }
        
        // Search filter
        if (search != null && !search.trim().isEmpty()) {
            String searchPattern = "%" + search.toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> 
                cb.or(
                    cb.like(cb.lower(root.get("title")), searchPattern),
                    cb.like(cb.lower(root.get("fileName")), searchPattern),
                    cb.like(cb.lower(root.get("targetRole").as(String.class)), searchPattern)
                ));
        }
        
        // Sorting
        Sort sortObj = switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "analyzedAt");
            case "score-high" -> Sort.by(Sort.Direction.DESC, "analysis.overallScore");
            case "score-low" -> Sort.by(Sort.Direction.ASC, "analysis.overallScore");
            default -> Sort.by(Sort.Direction.DESC, "analyzedAt");
        };
        
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), 
            pageable.getPageSize(), sortObj);
        
        return resumeReportRepository.findAll(spec, sortedPageable)
            .map(this::convertToResponse);
    }
    
    public ResumeReportResponse getReportById(String id, User user) {
        ResumeReport report = resumeReportRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        return convertToResponse(report);
    }
    
    @Transactional
    public void deleteReport(String id, User user) {
        ResumeReport report = resumeReportRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        fileStorageService.deleteFile(id, user);
        resumeReportRepository.delete(report);
    }
    
    @Transactional
    public ResumeReportResponse updateReport(String id, UpdateReportRequest request, User user) {
        ResumeReport report = resumeReportRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
        }
        
        resumeReportRepository.save(report);
        return convertToResponse(report);
    }
    
    private ResumeReportResponse convertToResponse(ResumeReport report) {
        ResumeReportResponse response = new ResumeReportResponse();
        response.setId(report.getId());
        response.setTitle(report.getTitle());
        response.setFileName(report.getFileName());
        response.setFileSize(report.getFileSize());
        response.setTargetRole(report.getTargetRole().name());
        response.setExperienceLevel(report.getExperienceLevel().name());
        response.setJobDescription(report.getJobDescription());
        
        // Parse JSON analysis
        try {
            ObjectMapper mapper = new ObjectMapper();
            response.setAnalysis(mapper.readValue(report.getAnalysisJson(), 
                AnalysisResultResponse.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse analysis data");
        }
        
        response.setCreatedAt(report.getCreatedAt().toString());
        response.setAnalyzedAt(report.getAnalyzedAt().toString());
        
        return response;
    }
}
```

### Configuration Classes

#### SecurityConfig
```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                .requestMatchers("/api/resume/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

#### WebClientConfig
```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .responseTimeout(Duration.ofMinutes(2))
                        .compress(true)))
                .build();
    }
}
```

### Environment Variables

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/apexhire
spring.datasource.username=apexhire
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# LangGraph Configuration
langgraph.api.url=http://localhost:8000
langgraph.api.key=your_langgraph_api_key

# File Storage Configuration
file.storage.location=./storage

# JWT Configuration
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# Server Configuration
server.port=9000
```

### Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResumeAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleResumeAnalysisException(
            ResumeAnalysisException ex) {
        ErrorResponse error = new ErrorResponse(
            "ANALYSIS_ERROR", 
            ex.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR", 
            message,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR", 
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Maven Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- WebClient for LangGraph -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

### Integration with Python LangGraph Backend

The Spring Boot backend communicates with the Python LangGraph backend using REST API calls:

#### LangGraph Request Format
```json
{
  "resume_text": "string",
  "target_role": "string",
  "experience_level": "string",
  "job_description": "string (optional)"
}
```

#### LangGraph Response Format
```json
{
  "overall_score": 85,
  "ats_score": 78,
  "job_match_score": 90,
  "summary": "string",
  "sections": [...],
  "strengths": [...],
  "weaknesses": [...],
  "missing_skills": [...],
  "missing_keywords": [...],
  "recommendations": [...],
  "improvements": [...]
}
```

### Testing Strategy

#### Unit Tests
- Service layer tests with mocked LangGraph responses
- Repository tests with test database
- DTO validation tests
- Exception handling tests

#### Integration Tests
- API endpoint tests with TestRestTemplate
- File upload/download tests
- LangGraph integration tests with mock server
- Database integration tests

#### Test Configuration
```java
@SpringBootTest
@AutoConfigureMockMvc
public class ResumeControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private LangGraphService langGraphService;
    
    @Test
    public void testAnalyzeResume() throws Exception {
        // Test implementation
    }
}
```

---

## Data Flow Diagram

### Current Client-Side Flow:
```
User Upload PDF
    ↓
Extract Text (pdf.js)
    ↓
User selects Role/Level + optional JD
    ↓
Call analyzeResume() → Lovable AI Gateway
    ↓
Receive AnalysisResult
    ↓
Create ResumeReport object
    ↓
Save to LocalStorage (metadata)
    ↓
Save to IndexedDB (PDF file)
    ↓
Navigate to Report Page
    ↓
Load from LocalStorage + IndexedDB
    ↓
Display AnalysisPanel + PdfViewer
```

### 3-Tier Architecture Flow (Frontend → Spring Boot → LangGraph):
```
User Upload PDF
    ↓
Extract Text (pdf.js)
    ↓
User selects Role/Level + optional JD
    ↓
Call Spring Boot → POST /api/resume/analyze
    ↓
Spring Boot validates request & authenticates user
    ↓
Spring Boot calls LangGraph Backend → POST /analyze
    ↓
LangGraph processes with AI models
    ↓
LangGraph returns AnalysisResult
    ↓
Spring Boot stores Report in database
    ↓
Spring Boot returns AnalysisResult to frontend
    ↓
Navigate to Report Page
    ↓
Load Report from Spring Boot → GET /api/resume/reports/:id
    ↓
Load PDF from Spring Boot → GET /api/resume/download/:id
    ↓
Display AnalysisPanel + PdfViewer
```

### Detailed Component Interactions:

**Resume Analysis Flow:**
```
Frontend (React)
    ↓ HTTP POST /api/resume/analyze
    ↓ {resumeText, targetRole, experienceLevel, jobDescription}
Spring Boot Controller
    ↓ Authenticate JWT token
    ↓ Validate request
Spring Boot Service
    ↓ Build LangGraph request
    ↓ HTTP POST to LangGraph Backend
Python LangGraph Backend
    ↓ Process with AI/LLM
    ↓ Generate structured analysis
    ↓ HTTP Response
Spring Boot Service
    ↓ Parse LangGraph response
    ↓ Create ResumeReport entity
    ↓ Save to database
    ↓ Return to frontend
Frontend
    ↓ Display results
```

**File Storage Flow:**
```
Frontend (React)
    ↓ HTTP POST /api/resume/upload
    ↓ MultipartFile (PDF)
Spring Boot Controller
    ↓ Authenticate JWT token
    ↓ Validate file (type, size)
Spring Boot FileStorageService
    ↓ Generate unique file ID
    ↓ Store in filesystem/S3
    ↓ Return file metadata
Frontend
    ↓ Store file ID for later reference
```

---

## Migration Notes

### Frontend Changes Required:

1. **Replace LocalStorage with API calls:**
   - Replace `loadReports()` with `GET /api/resume/reports`
   - Replace `saveReport()` with backend storage (automatic on analyze)
   - Replace `deleteReport()` with `DELETE /api/resume/reports/:id`
   - Replace `getReport()` with `GET /api/resume/reports/:id`

2. **Replace IndexedDB with API calls:**
   - Replace `saveResumeFile()` with `POST /api/resume/upload`
   - Replace `loadResumeFile()` with `GET /api/resume/download/:id`
   - Replace `deleteResumeFile()` with backend deletion (automatic on report delete)

3. **Update analyzeResume function:**
   - Change from calling Lovable AI directly to calling Spring Boot endpoint
   - Spring Boot will handle LangGraph integration
   - Remove `LOVABLE_API_KEY` dependency from frontend

4. **Authentication Integration:**
   - Ensure all API calls include Bearer token
   - Handle 401 errors with token refresh
   - Use existing `apiRequest` utility from `api-client.ts`

### Spring Boot Backend Requirements:

1. **Database Schema:**
   - `resume_reports` table: Store report metadata with analysis JSON
   - User association via foreign key to users table
   - JSON column for storing analysis results
   - File storage path column for PDF references

2. **LangGraph Integration:**
   - Implement WebClient-based communication with Python backend
   - Handle request/response transformation
   - Implement timeout and retry logic
   - Handle LangGraph API errors gracefully

3. **File Storage:**
   - Implement file upload handling with validation
   - Store files in filesystem or cloud storage (S3)
   - Implement file cleanup on report deletion
   - Generate unique file identifiers

4. **Security & Validation:**
   - JWT authentication for all endpoints
   - File type and size validation
   - Input parameter validation
   - User ownership verification for all operations

5. **Error Handling:**
   - Custom exceptions for resume analysis failures
   - Global exception handler with proper HTTP status codes
   - Logging of LangGraph communication errors
   - User-friendly error messages

### Python LangGraph Backend Requirements:

1. **API Endpoints:**
   - `POST /analyze` - Main analysis endpoint
   - Health check endpoint
   - Rate limiting and monitoring

2. **Request/Response Format:**
   - Accept JSON matching Spring Boot request format
   - Return JSON matching expected AnalysisResult structure
   - Validate input parameters

3. **AI Integration:**
   - Implement LangGraph workflow for resume analysis
   - Integrate with LLM providers (OpenAI, Anthropic, etc.)
   - Handle structured output generation
   - Implement caching for identical requests

4. **Performance:**
   - Implement async processing for long-running analyses
   - Request queuing and throttling
   - Timeout handling
   - Resource management

---

## Configuration

### Environment Variables

**Frontend (.env):**
- `VITE_API_BASE_URL`: Spring Boot backend URL (default: http://localhost:9000)

**Spring Boot Backend (application.properties):**
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/apexhire
spring.datasource.username=apexhire
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# LangGraph Configuration
langgraph.api.url=http://localhost:8000
langgraph.api.key=your_langgraph_api_key
langgraph.api.timeout=120000

# File Storage Configuration
file.storage.location=./storage
file.storage.max-size=10485760

# JWT Configuration
jwt.secret=your_jwt_secret_key
jwt.expiration=86400000

# Server Configuration
server.port=9000
```

**Python LangGraph Backend (.env):**
```properties
# LangGraph Configuration
LANGGRAPH_API_KEY=your_provider_api_key
LANGGRAPH_MODEL=gpt-4 or claude-3-opus
LANGGRAPH_TEMPERATURE=0.7

# Server Configuration
HOST=0.0.0.0
PORT=8000

# Optional: Provider-specific
OPENAI_API_KEY=your_openai_key
ANTHROPIC_API_KEY=your_anthropic_key
```

---

## Security Considerations

1. **File Upload Validation:**
   - Validate file type (PDF only)
   - Validate file size (max 10MB)
   - Scan for malware if possible

2. **AI API Security:**
   - Never expose AI API keys to frontend
   - Implement rate limiting on analysis endpoint
   - Monitor for abuse

3. **Data Privacy:**
   - Encrypt sensitive resume data at rest
   - Implement data retention policies
   - Allow users to delete their data

4. **Access Control:**
   - Users can only access their own reports
   - Implement proper authorization checks
   - Audit log access to sensitive data

---

## Performance Optimizations

1. **Caching:**
   - Cache analysis results for identical resumes
   - Cache frequently accessed reports
   - Implement CDN for file downloads

2. **Async Processing:**
   - Consider async analysis for large files
   - Implement job queue for AI processing
   - Provide progress updates

3. **Pagination:**
   - Already implemented in frontend
   - Ensure backend supports efficient pagination
   - Consider cursor-based pagination for large datasets

---

## Testing Requirements

### Unit Tests:
- Analysis schema validation
- Score calculation logic
- Data transformation functions

### Integration Tests:
- API endpoint testing
- File upload/download
- Analysis workflow end-to-end

### E2E Tests:
- Complete user flow from upload to report
- Error handling scenarios
- Cross-browser compatibility

---

## Future Enhancements

1. **Features:**
   - Resume comparison (compare two resumes)
   - Export reports to PDF
   - Share reports via link
   - Resume templates and suggestions
   - Integration with job boards

2. **AI Improvements:**
   - Industry-specific analysis
   - Company culture matching
   - Salary estimation based on resume
   - Interview question generation

3. **Analytics:**
   - Track improvement over time
   - Industry benchmarking
   - Success rate tracking

---

## File Reference Summary

### Core Files:
- `src/routes/resume-analyzer.tsx` - Main analyzer page
- `src/routes/resume-report.$id.tsx` - Individual report page
- `src/routes/resume-reports.tsx` - All reports listing
- `src/components/resume/analysis-panel.tsx` - Analysis display
- `src/components/resume/report-row.tsx` - Report card component
- `src/components/resume/pdf-viewer.tsx` - PDF viewing component

### Library Files:
- `src/lib/resume-reports.ts` - Data structures and storage
- `src/lib/resume-analysis.server.ts` - AI analysis logic
- `src/lib/resume-analysis.schema.ts` - Validation schemas
- `src/lib/resume-analysis.functions.ts` - Server function wrapper
- `src/lib/api-client.ts` - API client utilities

---

## Python LangGraph Backend Specifications

### Architecture Overview
The Python LangGraph backend handles the AI-powered resume analysis using LangGraph workflows and LLM integration.

### Technology Stack
- **Framework:** FastAPI or Flask
- **Language:** Python 3.9+
- **AI/ML:** LangGraph, LangChain
- **LLM Providers:** OpenAI, Anthropic, or others
- **Data Processing:** Pydantic for validation
- **Async Support:** asyncio for concurrent processing

### Project Structure
```
langgraph-resume-analyzer/
├── app/
│   ├── main.py              # FastAPI application
│   ├── config.py            # Configuration management
│   ├── models.py            # Pydantic models
│   ├── workflows/
│   │   ├── __init__.py
│   │   ├── resume_analysis.py  # LangGraph workflow
│   │   └── agents.py            # AI agents
│   ├── services/
│   │   ├── llm_service.py      # LLM integration
│   │   └── parser_service.py   # Response parsing
│   └── utils/
│       ├── validation.py       # Input validation
│       └── formatting.py       # Output formatting
├── tests/
├── requirements.txt
└── .env
```

### API Endpoint Implementation

#### Main Analysis Endpoint
```python
from fastapi import FastAPI, HTTPException, BackgroundTasks
from pydantic import BaseModel
from app.workflows.resume_analysis import run_resume_analysis

app = FastAPI(title="Resume Analysis API")

class AnalyzeRequest(BaseModel):
    resume_text: str
    target_role: str
    experience_level: str
    job_description: str = None

class AnalysisResponse(BaseModel):
    overall_score: int
    ats_score: int
    job_match_score: int = None
    summary: str
    sections: list
    strengths: list
    weaknesses: list
    missing_skills: list
    missing_keywords: list
    recommendations: list
    improvements: list

@app.post("/analyze", response_model=AnalysisResponse)
async def analyze_resume(request: AnalyzeRequest):
    try:
        result = await run_resume_analysis(
            resume_text=request.resume_text,
            target_role=request.target_role,
            experience_level=request.experience_level,
            job_description=request.job_description
        )
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.get("/health")
async def health_check():
    return {"status": "healthy"}
```

### LangGraph Workflow Implementation

#### Resume Analysis Workflow
```python
from langgraph.graph import StateGraph, END
from typing import TypedDict, List
from app.services.llm_service import get_llm_response

class AnalysisState(TypedDict):
    resume_text: str
    target_role: str
    experience_level: str
    job_description: str
    overall_score: int
    ats_score: int
    job_match_score: int
    summary: str
    sections: List[dict]
    strengths: List[str]
    weaknesses: List[str]
    missing_skills: List[str]
    missing_keywords: List[str]
    recommendations: List[dict]
    improvements: List[str]

def analyze_overall(state: AnalysisState) -> AnalysisState:
    """Analyze overall resume quality"""
    prompt = f"""
    Analyze the overall quality of this resume for a {state['target_role']} position 
    ({state['experience_level']}).
    
    Resume: {state['resume_text'][:2000]}
    """
    response = get_llm_response(prompt)
    state['overall_score'] = response.get('score', 75)
    state['summary'] = response.get('summary', '')
    return state

def analyze_ats_compatibility(state: AnalysisState) -> AnalysisState:
    """Analyze ATS compatibility"""
    prompt = f"""
    Evaluate ATS compatibility for this resume targeting {state['target_role']}.
    Focus on: formatting, keywords, structure.
    
    Resume: {state['resume_text'][:2000]}
    """
    response = get_llm_response(prompt)
    state['ats_score'] = response.get('score', 70)
    return state

def analyze_job_match(state: AnalysisState) -> AnalysisState:
    """Analyze job description match if provided"""
    if not state['job_description']:
        state['job_match_score'] = None
        return state
    
    prompt = f"""
    Compare this resume to the job description:
    
    Resume: {state['resume_text'][:2000]}
    Job Description: {state['job_description'][:2000]}
    """
    response = get_llm_response(prompt)
    state['job_match_score'] = response.get('score', 80)
    return state

def analyze_sections(state: AnalysisState) -> AnalysisState:
    """Analyze individual resume sections"""
    sections = ['skills', 'keywords', 'experience', 'education', 
                'projects', 'content', 'formatting']
    
    section_results = []
    for section in sections:
        prompt = f"""
        Analyze the {section} section of this resume for {state['target_role']}.
        Resume: {state['resume_text'][:2000]}
        """
        response = get_llm_response(prompt)
        section_results.append({
            'key': section,
            'title': section.title(),
            'score': response.get('score', 70),
            'summary': response.get('summary', ''),
            'points': response.get('points', [])
        })
    
    state['sections'] = section_results
    return state

def identify_strengths_weaknesses(state: AnalysisState) -> AnalysisState:
    """Identify strengths and weaknesses"""
    prompt = f"""
    Identify 4-6 strengths and 4-6 weaknesses of this resume for {state['target_role']}.
    Resume: {state['resume_text'][:3000]}
    """
    response = get_llm_response(prompt)
    state['strengths'] = response.get('strengths', [])
    state['weaknesses'] = response.get('weaknesses', [])
    return state

def identify_missing_elements(state: AnalysisState) -> AnalysisState:
    """Identify missing skills and keywords"""
    prompt = f"""
    Identify missing skills and keywords for a {state['target_role']} resume.
    Resume: {state['resume_text'][:3000]}
    """
    response = get_llm_response(prompt)
    state['missing_skills'] = response.get('missing_skills', [])
    state['missing_keywords'] = response.get('missing_keywords', [])
    return state

def generate_recommendations(state: AnalysisState) -> AnalysisState:
    """Generate prioritized recommendations"""
    prompt = f"""
    Generate 4-6 specific recommendations to improve this resume for {state['target_role']}.
    Prioritize as high, medium, or low.
    Resume: {state['resume_text'][:3000]}
    """
    response = get_llm_response(prompt)
    state['recommendations'] = response.get('recommendations', [])
    return state

def generate_improvements(state: AnalysisState) -> AnalysisState:
    """Generate suggested improvements"""
    prompt = f"""
    Provide 5-8 specific improvements for this resume targeting {state['target_role']}.
    Resume: {state['resume_text'][:3000]}
    """
    response = get_llm_response(prompt)
    state['improvements'] = response.get('improvements', [])
    return state

# Build the workflow graph
workflow = StateGraph(AnalysisState)

workflow.add_node("analyze_overall", analyze_overall)
workflow.add_node("analyze_ats", analyze_ats_compatibility)
workflow.add_node("analyze_job_match", analyze_job_match)
workflow.add_node("analyze_sections", analyze_sections)
workflow.add_node("identify_swot", identify_strengths_weaknesses)
workflow.add_node("identify_missing", identify_missing_elements)
workflow.add_node("generate_recommendations", generate_recommendations)
workflow.add_node("generate_improvements", generate_improvements)

# Define the execution order
workflow.set_entry_point("analyze_overall")
workflow.add_edge("analyze_overall", "analyze_ats")
workflow.add_edge("analyze_ats", "analyze_job_match")
workflow.add_edge("analyze_job_match", "analyze_sections")
workflow.add_edge("analyze_sections", "identify_swot")
workflow.add_edge("identify_swot", "identify_missing")
workflow.add_edge("identify_missing", "generate_recommendations")
workflow.add_edge("generate_recommendations", "generate_improvements")
workflow.add_edge("generate_improvements", END)

# Compile the workflow
app_graph = workflow.compile()

async def run_resume_analysis(resume_text: str, target_role: str, 
                              experience_level: str, job_description: str = None):
    """Execute the resume analysis workflow"""
    initial_state = {
        "resume_text": resume_text,
        "target_role": target_role,
        "experience_level": experience_level,
        "job_description": job_description,
        "overall_score": 0,
        "ats_score": 0,
        "job_match_score": 0,
        "summary": "",
        "sections": [],
        "strengths": [],
        "weaknesses": [],
        "missing_skills": [],
        "missing_keywords": [],
        "recommendations": [],
        "improvements": []
    }
    
    final_state = await app_graph.ainvoke(initial_state)
    return final_state
```

### LLM Service Integration

#### LLM Service
```python
from langchain_openai import ChatOpenAI
from langchain_anthropic import ChatAnthropic
from app.config import settings

def get_llm_provider():
    """Get configured LLM provider"""
    if settings.provider == "openai":
        return ChatOpenAI(
            model=settings.model,
            temperature=settings.temperature,
            api_key=settings.openai_api_key
        )
    elif settings.provider == "anthropic":
        return ChatAnthropic(
            model=settings.model,
            temperature=settings.temperature,
            api_key=settings.anthropic_api_key
        )
    else:
        raise ValueError(f"Unsupported provider: {settings.provider}")

def get_llm_response(prompt: str, response_format: dict = None) -> dict:
    """Get structured response from LLM"""
    llm = get_llm_provider()
    
    if response_format:
        # Use structured output
        structured_llm = llm.with_structured_output(response_format)
        response = structured_llm.invoke(prompt)
        return response.dict()
    else:
        # Use text response
        response = llm.invoke(prompt)
        return {"content": response.content}
```

### Configuration Management

#### Config
```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # API Configuration
    host: str = "0.0.0.0"
    port: int = 8000
    
    # LLM Configuration
    provider: str = "openai"  # openai, anthropic
    model: str = "gpt-4"
    temperature: float = 0.7
    
    # API Keys
    openai_api_key: str = ""
    anthropic_api_key: str = ""
    
    # LangGraph Configuration
    langgraph_api_key: str = ""
    
    class Config:
        env_file = ".env"

settings = Settings()
```

### Requirements
```
fastapi==0.104.1
uvicorn==0.24.0
pydantic==2.5.0
pydantic-settings==2.1.0
langchain==0.1.0
langchain-openai==0.0.2
langchain-anthropic==0.0.1
langgraph==0.0.20
python-multipart==0.0.6
```

### Error Handling & Monitoring

#### Exception Handlers
```python
from fastapi import Request
from fastapi.responses import JSONResponse

@app.exception_handler(ValueError)
async def value_error_handler(request: Request, exc: ValueError):
    return JSONResponse(
        status_code=400,
        content={"detail": str(exc)}
    )

@app.exception_handler(Exception)
async def general_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"}
    )
```

### Performance Optimization

1. **Caching:** Cache results for identical resume analyses
2. **Async Processing:** Use async/await for concurrent LLM calls
3. **Request Queuing:** Implement queue system for high load
4. **Timeout Handling:** Set appropriate timeouts for LLM calls
5. **Resource Management:** Limit concurrent requests to prevent overload

---

## Conclusion

The current implementation uses client-side storage (LocalStorage + IndexedDB) and direct AI integration. To move to a production-ready 3-tier architecture, the following components should be implemented:

1. **Spring Boot Backend:** Acts as API gateway, handles authentication, file storage, and database operations while communicating with the Python LangGraph backend.

2. **Python LangGraph Backend:** Handles AI-powered resume analysis using LangGraph workflows and LLM integration, providing structured analysis results.

3. **Frontend Migration:** Replace client-side storage with API calls to the Spring Boot backend while maintaining the same data structures and user experience.

The frontend components are well-structured and can be adapted to work with the new backend architecture by replacing the storage functions with API calls. The Spring Boot backend provides a robust, secure middleware layer, while the Python LangGraph backend offers flexible AI processing capabilities.