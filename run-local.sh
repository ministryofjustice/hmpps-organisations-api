#
# This script is used to run the Organisations API locally.
#
# It runs with a combination of properties from the default spring profile (in application.yaml) and supplemented
# with the -local profile (from application-local.yml). The latter overrides some of the defaults.
#
# The environment variables here will also override values supplied in spring profile properties, specifically
# around setting the DB properties, SERVER_PORT and client credentials to match those used in the docker-compose files.
#

# Provide the DB connection details to local container-hosted Postgresql DB already running
export DB_SERVER=localhost
export DB_NAME=organisations-db
export DB_USER=organisations
export DB_PASS=organisations
export DB_SSL_MODE=prefer

export API_BASE_URL_HMPPS_AUTH=https://sign-in-dev.hmpps.service.justice.gov.uk/auth

export $(cat .env | xargs)  # If you want to set or update the current shell environment e.g. system client and secret.

# Run the application with stdout and local profiles active
SPRING_PROFILES_ACTIVE=stdout,local ./gradlew bootRun

# End