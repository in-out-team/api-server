# In-Out Api-Server

## Configuration

### Environment Variables

- DB_URL: Database URL
  - ex) jdbc:postgresql://localhost:5432/in-out
- DB_USERNAME: Database username
  - ex) postgres
- DB_PASSWORD: Database password
  - ex) postgres
- JWT_KEY: JWT Secret Key
  - ex) super_long_secret_key
  - must be at least 256 bits long 
- OPENAI_API_KEY: Open API Key
  - ex) open_api_key
- OPENAI_ORGANIZATION: OpenAI Organization
  - ex) org-123456
- GOOGLE_IOS_CLIENT_ID: Google OAuth Client ID for iOS
  - ex) 111111111111-aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa.apps.googleusercontent.com
- AWS_S3_ACCESS_KEY: AWS S3 Access Key
  - ex) AKIAAAAAAAAAAAAAAAAA
- AWS_S3_SECRET_KEY: AWS S3 Secret Key
  - ex) aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
- JOBRUNR_DB_DATASOURCE: JobRunr Database URL
  - ex) jdbc:postgresql://localhost:5432/in-out-jobrunr-local

## Next Steps

- change classes mapped to entities from data classes to regular classes
  - [ref link](https://spoqa.github.io/2022/08/16/kotlin-jpa-entity.html)
- add logging & monitoring
  - openai usage (who used, how many(tokens), and when)
  - Sentry
  - GCP Cloud Logging
  - GCP Cloud Monitoring
- rate limit
  - openai request rate limit
- deploy on k8s
  - add CD (CI already setup with GitHub Actions)
