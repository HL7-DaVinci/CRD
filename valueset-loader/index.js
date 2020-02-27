const readline = require('readline')
const passwordPrompt = require('password-prompt')

function askForUMLSCredentials() {
  return new Promise(resolve => {
    const rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout
    })

    rl.question('UMLS Username: ', username => {
      rl.close()

      passwordPrompt("UMLS Password: ", { method: "hide" }).then(password => {
        resolve({ username: username, password: password })
      })
    })
  })
}

askForUMLSCredentials().then(creds => {
  console.log("hello " + creds.username)
})