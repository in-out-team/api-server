# In-Out Api-Server

## Configuration

### Environment Variables

- MONGODB_URI: Database URL
  - ex) mongodb://root:example@in-out-db:27017/local-in-out?authSource=admin
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
- JOBRUNR_DB_URI: JobRunr Database URL
  - ex) mongodb://root:example@in-out-db:27017/local-in-out-jobrunr?authSource=admin

## Next Steps

- add logging & monitoring
  - openai usage (who used, how many(tokens), and when)
  - Sentry
  - GCP Cloud Logging
  - GCP Cloud Monitoring
- rate limit
  - openai request rate limit
- deploy on k8s
  - add CD (CI already setup with GitHub Actions)
