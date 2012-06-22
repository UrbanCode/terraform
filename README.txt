USE:

export $TERRAFORM_HOME={your terraform directory}
./bin/terraform create [input-xml-file] [input-credentials-file] [prop1=val1 prop2=val2 ... ]

Example Templates and Credentials are stored in example-config/


EC2:
In order for the ssh post create actions to work (this is not the user-data actions) you will need the ssh-keys that the configured instance was started with. These should be placed in ~/.terraform as the .pem files that you get from Amazon.

VMWare: 
copy files from example-config/vm-conf/ to ~/.terraform

use terraform.sh in the bin directory to create or destroy an environment.
param1 : create or destroy
param2 : path to input xml file. Template if creating; instance if destroying
param3 : path to credentials file
