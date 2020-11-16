set -x

source conjur_utils.sh

export CONTAINER_NAME="conjur-master"
export ADMIN_PASSWORD="CYberark11@@"
export CONJUR_ACCOUNT="conjur"

docker-compose exec -T dap evoke configure master --accept-eula --hostname $CONTAINER_NAME --admin-password $ADMIN_PASSWORD $CONJUR_ACCOUNT

export CONJUR_APPLIANCE_URL="https://conjur-master"
export CONJUR_AUTHN_LOGIN="admin"
export CONJUR_AUTHN_API_KEY="${ADMIN_PASSWORD}"

ls
response=$(conjur_append_policy "root" ./root.yml)
teamcity_project_api_key=$(echo "${response}" | jq -r ".created_roles" | jq -r '.["conjur:host:teamcity/projectName"]' | jq -r .api_key)

echo "Teamcity Project API Key: ${teamcity_project_api_key}"
