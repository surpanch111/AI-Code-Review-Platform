# AI-Powered Code Review Assistant

Companion project for the article "AI-Powered Code Review Assistant: Automated Code Analysis with Spring AI and MongoDB" published on [Foojay](https://foojay.io/).

## Prerequisites

- Java 21 or later
- Maven 3.9+
- A MongoDB Atlas cluster with Atlas Vector Search enabled
- An OpenAI API key

## Setup

1. Clone the repository and navigate to the project directory.

2. Set environment variables:

```bash
export MONGODB_URI="mongodb+srv://<username>:<password>@<cluster>.mongodb.net/code-review-assistant?appName=devrel-article-java-springai-codereview"
export OPENAI_API_KEY="your-openai-api-key"
```

3. Create a Vector Search index on the `review_patterns` collection in Atlas:

   - Go to your cluster in the Atlas UI
   - Select the **Atlas Search** tab
   - Click **Create Search Index**
   - Choose **Atlas Vector Search**
   - Select the `review_patterns` collection
   - Name the index `vector_index`
   - Use this definition:

```json
{
  "fields": [
    {
      "type": "vector",
      "path": "embedding",
      "numDimensions": 1536,
      "similarity": "cosine"
    }
  ]
}
```

4. Build and run:

```bash
mvn spring-boot:run
```

The `DataSeeder` loads about 20 Java anti-patterns with embeddings on first startup.

## API endpoints

| Method | Endpoint                              | Description                        |
|--------|---------------------------------------|------------------------------------|
| POST   | `/api/patterns`                       | Add a new review pattern           |
| GET    | `/api/patterns`                       | List patterns (filter by language/category) |
| GET    | `/api/patterns/{id}`                  | Get a single pattern               |
| POST   | `/api/reviews`                        | Submit code for review             |
| GET    | `/api/reviews/{submissionId}`         | Get a past review with findings    |
| GET    | `/api/reviews/{submissionId}/findings`| Get findings for a submission      |
| GET    | `/api/analytics/categories`           | Finding counts by category         |
| GET    | `/api/analytics/severity`             | Finding counts by severity         |
| GET    | `/api/analytics/top-patterns`         | Most frequently triggered patterns |

## Example usage

Submit code for review:

```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Content-Type: application/json" \
  -d '{
    "code": "public void process() {\n    FileInputStream fis = new FileInputStream(\"data.txt\");\n    byte[] data = fis.readAllBytes();\n}",
    "language": "java"
  }'
```
