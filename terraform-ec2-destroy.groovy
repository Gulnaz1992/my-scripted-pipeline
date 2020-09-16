properties([
    parameters([
        booleanParam(defaultValue: false, description: 'Do you want to run terrform destroy', name: 'terraform_destroy'),
        string(defaultValue: '', description: 'Provide SOURCE_PROJECT_NAME', name: 'SOURCE_PROJECT_NAME', trim: true)
    ])
])

def aws_region_var = ''
def environment = ''

if(params.SOURCE_PROJECT_NAME ==~ "dev.*"){
    aws_region_var = "us-east-1"
    environment = 'dev'
}
else if(params.SOURCE_PROJECT_NAME ==~ "qa.*"){
    aws_region_var = "us-east-2"
    environment = 'qa'
}
else if(params.SOURCE_PROJECT_NAME ==~ "master"){
    aws_region_var = "us-west-2"
    environment = 'prod'
}
else {
    error("SOURCE_PROJECT_NAME Name Doesnt Match RegEx")
}

def tf_vars = """
    s3_bucket = \"jenkins-terraform-apyzova\"
    s3_folder_project = \"terraform_ec2\"
    s3_folder_region = \"us-east-1\"
    s3_folder_type = \"class\"
    s3_tfstate_file = \"infrastructure.tfstate\"
    environment = \"${environment}\"
    region      = \"${aws_region_var}\"
    public_key  = \"ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQDZQ4I2XdgfJGyF/2JozGi4wA6lrfjZgigN2MLPcv+g3464KeuhBXGpXjtldXnqwd4VePiy1salPTYxvN6P9W2/ilijY3jcBznxe1vixYlnNqMYokB1t/In+Qgc0NLdL2QecWfeeG4gIgupP4MpqFVgJ+ZceN94WSSSnHzVb5V94g/o15KcASNFcDLYGykXZ7EsfUnSzY8fFA6yRrdl5lrI5iBnYsuMUSvOkyCqJ/MnuxqtS0hkBGpPvFObDr9k19RgnaY4sDaYBdCuZNfAnN8Ht7XDEYUqNHPkMQgZIpGK8aiTIS1L9Sr6iunLuIJmcRchwU8ckwybtz7y8Opqj7teuKJT+3oLB1x3mkpnQwAF7i8DRmGeC11LEOSLF4zuchgm6NXjud2eDjjYMPAcgimvO7nFRgltdyq3GXv8/gSFmnAos5lXzjwn0IOa3BIzMGmf86cSiZgB4ksRi9R49wniC7XssdxIS+/v/dtxGzaV1hB72DGnqeFPdCpQX/tOJrc= gulnaz@Gulnazs-MBP"
    ami_id      = \"*\"
"""

node{
    stage("Pull Repo"){
        cleanWs()
        git url: 'https://github.com/Gulnaz1992/terraform-ec2.git'
    }

    withCredentials([usernamePassword(credentialsId: 'jenkins-aws-access-key', passwordVariable: 'AWS_SECRET_ACCESS_KEY', usernameVariable: 'AWS_ACCESS_KEY_ID')]) {
        withEnv(["AWS_REGION=${aws_region_var}"]) {
            stage("Terrraform Init"){
                writeFile file: "${environment}.tfvars", text: "${tf_vars}"
                sh """
                    bash setenv.sh ${environment}.tfvars
                    terraform-0.13 init
                """
            }        
            
            if (terraform_destroy.toBoolean()) {
                stage("Terraform Destroy"){
                    sh """
                        terraform-0.13 destroy -var-file ${environment}.tfvars -auto-approve
                    """
                }
            }
            else {
                stage("Terraform Plan"){
                    sh """
                        terraform-0.13 plan -var-file ${environment}.tfvars
                    """
                }
            }
        }        
    }    
}