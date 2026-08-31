# Resume API Request Body Samples for Swagger

## JSON Request Bodies

### 1. Update Report (PUT)
**Endpoint:** `PUT /api/resumes/reports/{id}`  
**Content-Type:** `application/json`  
**Authentication:** Bearer Token required

```json
{
  "title": "Updated Resume Title"
}
```

**Example values:**
```json
{
  "title": "Software Engineer Resume - John Doe"
}
```

```json
{
  "title": "Senior Backend Developer Resume"
}
```

---

## Form Data Request Bodies (for file upload endpoints)

### 2. Analyze Resume (POST)
**Endpoint:** `POST /api/resumes/analyze`  
**Content-Type:** `multipart/form-data`  
**Authentication:** Bearer Token required

**Form fields:**
- `file`: [PDF file] - Required
- `targetRole`: String - Required
- `experienceLevel`: String - Required
- `resumeTitle`: String - Optional
- `jobDescription`: String - Optional

**Example values for form fields:**
```
file: [select PDF file]
targetRole: "Software Engineer"
experienceLevel: "Mid-Level"
resumeTitle: "My Resume - John Doe"
jobDescription: "We are looking for a software engineer with experience in Java, Spring Boot, and cloud technologies."
```

### 3. Upload Resume File (POST)
**Endpoint:** `POST /api/resumes/upload`  
**Content-Type:** `multipart/form-data`  
**Authentication:** Bearer Token required

**Form fields:**
- `file`: [PDF file] - Required

**Example values for form fields:**
```
file: [select PDF file]
```

---

## Query Parameters (for GET endpoints)

### 4. Get All Reports (GET)
**Endpoint:** `GET /api/resumes/reports`  
**Authentication:** Bearer Token required

**Query parameters:**
- `page`: Integer (optional, default: 0)
- `limit`: Integer (optional, default: 10)
- `role`: String (optional) - Filter by target role
- `level`: String (optional) - Filter by experience level
- `search`: String (optional) - Search in title or file name
- `sort`: String (optional, default: "newest") - Sort order: "newest", "oldest", "title-asc", "title-desc"

**Example query strings:**
```
?page=0&limit=10
?page=0&limit=10&role=Software%20Engineer&level=Mid-Level
?page=0&limit=10&search=Java&sort=newest
```

---

## Path Parameters

### 5. Get Report by ID (GET)
**Endpoint:** `GET /api/resumes/reports/{id}`  
**Authentication:** Bearer Token required

**Path parameter:**
- `id`: String (MongoDB ObjectId)

**Example:**
```
GET /api/resumes/reports/507f1f77bcf86cd799439011
```

### 6. Download Resume File (GET)
**Endpoint:** `GET /api/resumes/reports/{id}/file`  
**Authentication:** Bearer Token required

**Path parameter:**
- `id`: String (MongoDB ObjectId)

**Example:**
```
GET /api/resumes/reports/507f1f77bcf86cd799439011/file
```

### 7. Delete Report (DELETE)
**Endpoint:** `DELETE /api/resumes/reports/{id}`  
**Authentication:** Bearer Token required

**Path parameter:**
- `id`: String (MongoDB ObjectId)

**Example:**
```
DELETE /api/resumes/reports/507f1f77bcf86cd799439011
```

---

## Sample Values for Testing

### Target Roles
```json
"Software Engineer"
"Senior Software Engineer"
"Full Stack Developer"
"Backend Developer"
"Frontend Developer"
"DevOps Engineer"
"Data Engineer"
"Machine Learning Engineer"
"Product Manager"
"Technical Lead"
```

### Experience Levels
```json
"Entry-Level"
"Junior"
"Mid-Level"
"Senior"
"Lead"
"Principal"
"Executive"
```

### Sample Job Descriptions
```json
"We are looking for a skilled Software Engineer to join our team. Requirements: 3+ years of experience in Java development, strong knowledge of Spring Boot framework, experience with RESTful API design, familiarity with cloud platforms (AWS/GCP/Azure), proficiency in database technologies (PostgreSQL, MongoDB), good understanding of microservices architecture, excellent problem-solving skills."
```

```json
"Seeking a Senior Backend Developer with 5+ years of experience. Must have expertise in Java/Kotlin, Spring Boot, microservices architecture, PostgreSQL, Redis, Kubernetes, and CI/CD pipelines. Experience with event-driven architecture and message brokers (Kafka/RabbitMQ) is a plus."
```

### Sample Resume Titles
```json
"Software Engineer Resume - John Doe"
"Senior Full Stack Developer Resume"
"Backend Developer Resume - Jane Smith"
"DevOps Engineer Resume - Mike Johnson"
"Data Engineer Resume - Sarah Williams"
```

---

## Notes
- All endpoints require Bearer token authentication (JWT)
- File uploads must be PDF format
- Maximum file size: 10MB (configurable)
- The `analyze` endpoint performs both file upload and analysis in one request
- The `upload` endpoint only stores the file without analysis
- Report IDs are MongoDB ObjectId strings (24-character hexadecimal)
- Only the PUT endpoint accepts JSON body; file upload endpoints use multipart/form-data
