JBehave Story - Unit tests

Scenario: Testing an internal server error - 500
Given path 'error'
When method GET
Then status 500

Scenario: Testing exact response of account GET endpoint read json
Given path 'v1/accounts/0123456789'
When method GET
Then status 200
And match response '/data/unitAccountResponse.json'

Scenario: Testing exact response of accounts GET endpoint read json
Given path 'v1/accounts'
When method GET
Then status 200
And match response '/data/unitAccountsResponse.json'

Scenario: Testing exact response of account GET endpoint for {accountId: '0123456789'}
Given path 'v1/accounts/0123456789'
When method GET
Then status 200
And match response.accounts contains expected
| accountId    | accountType | accountOpenDate  | accountCloseDate |        
| '0123456789' | 'debit'     | '01/01/2020'     | '31/12/2020'     |

Scenario: Testing exact response of accounts GET endpoint
Given path 'v1/accounts'
When method GET
Then status 200
And match response.accounts contains expected
| accountId    | accountType | accountOpenDate  | accountCloseDate |        
| '0123456789' | 'debit'     | '01/01/2020'     | '31/12/2020'     |