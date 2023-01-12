package _Self.buildTypes

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object Listuser3 : BuildType({
    name = "listuser3"

    artifactRules = "/"
    publishArtifacts = PublishMode.SUCCESSFUL

    params {
        password("env.CLIENT_SECRET", "******", display = ParameterDisplay.HIDDEN, readOnly = true)
        password("env.CLIENT_ID", "******", display = ParameterDisplay.HIDDEN, readOnly = true)
    }

    steps {
        script {
            scriptContent = """
                #!/bin/bash
                
                CLIENT_ID=%env.CLIENT_ID%
                CLIENT_SECRET=%env.CLIENT_SECRET%
                
                TOKEN_ENDPOINT=https://login.microsoftonline.com/e12daa31-a171-4a62-89f6-4e78cd972d5f/oauth2/token
                GROUP_ID=12a0da3b-8545-42d9-9416-8abae28c1764
                
                # Get access token
                RESPONSE=${'$'}(curl -s --request POST -H  "Content-Type: application/x-www-form-urlencoded" ${'$'}TOKEN_ENDPOINT --data "grant_type=client_credentials&client_id=${'$'}CLIENT_ID&client_secret=${'$'}CLIENT_SECRET"  --data-urlencode 'resource=https://graph.microsoft.com')
                ACCESS_TOKEN=${'$'}(echo "${'$'}RESPONSE" | sed "s/{.*\"access_token\":\"\([^\"]*\).*}/\1/g")
                echo "${'$'}ACCESS_TOKEN"
                
                # List group members
                #MEMBERS_RESPONSE=${'$'}(curl -H  "Authorization: Bearer ${'$'}ACCESS_TOKEN"     #https://graph.microsoft.com/v1.0/groups/${'$'}GROUP_ID/members?${'$'}expand=user&${'$'}top=100&${'$'}skip=100)
                # Set the initial skip value
                skip=0
                
                # Set the maximum number of users to retrieve per page
                top=100
                
                while true; do
                # Make a GET request to the Graph API to retrieve the users in the group
                response=${'$'}(curl -X GET "https://graph.microsoft.com/v1.0/groups/${'$'}GROUP_ID/members?${'$'}expand=user&${'$'}top=${'$'}top&${'$'}skip=${'$'}skip" -H "Authorization: Bearer ${'$'}ACCESS_TOKEN")
                
                # Extract the display names of the users from the JSON response
                display_names=${'$'}(echo ${'$'}response | grep -o '"displayName": "[^"]*' | sed 's/"displayName": "//')
                echo ${'$'}display_names
                # Print the display names, one per line
                #echo ${'$'}display_names | awk '{print ${'$'}1}'
                
                # Increment the skip value by the number of users retrieved
                skip=${'$'}((skip + top))
                
                # Check if the "odata.nextLink" property is empty
                if ! grep -q '"odata.nextLink":' <<< "${'$'}response"; then
                # If the "odata.nextLink" property is empty, then there are no more users to retrieve
                break
                fi
                done
                
                
                #echo "${'$'}MEMBERS_RESPONSE"
                # Extract email addresses
                #EMAILS=${'$'}(echo "${'$'}MEMBERS_RESPONSE" |  grep -oP '"mail:"\K[^"]+')
                #EMAILS=${'$'}(echo "${'$'}MEMBERS_RESPONSE" | grep -oP '"mail":.*' | cut -d ':' -f2)
                #EMAILS=${'$'}(echo "${'$'}MEMBERS_RESPONSE" |  grep -oP '"userPrincipalName":\s*([^,]+)')
                
                # Print email addresses
                echo "LIST OF USERS"
                
                #echo "${'$'}EMAILS"
                #touch list.txt
                #echo "${'$'}EMAILS" < list.txt
            """.trimIndent()
        }
    }
})
