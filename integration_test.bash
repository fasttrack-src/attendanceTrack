curl -H "Content-type:application/json" -X POST 127.0.0.1:8080/rest/login --data '{"guid": "admin", "password": "alwyn96327011"}' -c - | awk '{print $7}' | tail -n 1 > token
token=$(cat token)
curl -H "Content-type:application/json" -X POST 127.0.0.1:8080/rest/getGroups --data '{"guid": "admin"}' --cookie "token=$token" > answer
cat answer
if grep -q CS1P answer; then echo "0"; else exit 1; fi