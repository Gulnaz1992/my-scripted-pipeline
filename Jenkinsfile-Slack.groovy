node {
  stage("Clone Repo"){
    git  'https://github.com/Gulnaz1992/my-scripted-pipeline.git'
  }
  stage("Testing"){
    echo 'Testing..'
  }
  stage("Deploying "){
    echo 'Deploying....'
  }
  stage("Send Notification to Slack"){
    slackSend channel: 'apr_2020', message: 'Hello'
  }
}