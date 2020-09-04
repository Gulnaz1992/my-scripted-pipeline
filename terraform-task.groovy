node {
    stage("Pull Repo"){
        ssh 'echo "{Pulling Repo"'
    }

    stage ("Pull Repo"){
        ssh 'echo "Terraform Init"'
    }

    stage("Terraform Apply"){
        ssh 'echo "Terraform Apply"'
    }

}