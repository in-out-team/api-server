#!/bin/bash
set -e

# Check if an email argument is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <email>"
  exit 1
fi

EMAIL="$1"

# Run SQL query inside the Docker container
docker exec -it in-out-db psql -U admin -d in-out -c "
    UPDATE users
    SET roles = (
        SELECT jsonb_agg(DISTINCT role)
        FROM jsonb_array_elements(roles || '[\"ADMIN\"]'::jsonb) AS role
    )
    WHERE email = '$EMAIL';
"

echo "Admin role granted to user: $EMAIL"
