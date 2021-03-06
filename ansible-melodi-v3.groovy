properties([
    parameters([
        string(defaultValue: '', description: 'Please enter VM IP', name: 'nodeIP', trim: true),
        string(defaultValue: '', description: 'Please enter branch name', name: 'branch', trim: true)
        ])
    ])
if (nodeIP?.trim()) {
    node {
        withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
            stage('Pull Repo') {
                git branch: '${branch}', changelog: false, poll: false, url: 'https://github.com/ikambarov/ansible-melodi.git'
            }
            withCredentials([sshUserPrivateKey(credentialsId: 'jenkins-master-ssh-key', keyFileVariable: 'SSHKEY', passphraseVariable: '', usernameVariable: 'SSHUSERNAME')]) {
                stage("Install Apache"){
                    sh '''
                        export ANSIBLE_HOST_KEY_CHECKING=False
                        ansible-playbook -i "104.131.81.80," --private-key $SSHKEY main.yml
                        '''
                }
            }
        }
    }
}
else {
    error 'Please enter valid IP address'
}