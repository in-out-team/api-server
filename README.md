# In-Out Api-Server

## Configuration

### Environment Variables

- DB_URL: Database URL, ex) jdbc:postgresql://localhost:5432/in_out
- DB_USERNAME: Database username, ex) postgres
- DB_PASSWORD: Database password, ex) postgres
- JWT_KEY: JWT Secret Key, ex) super_long_secret_key
  - must be at least 256 bits long 

## TODO

- [ ] Set Timezone to KST in Spring Boot and PostgreSQL