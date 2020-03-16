if bitbucket_server.pr_body.length < 5
    warn "Please provide a summary in the Pull Request description."
  end
  
  android_lint.severity = "Error"
  android_lint.skip_gradle_task = true
  Dir["app/build/reports/lint-results.xml"].each do |file|
    android_lint.report_file = file
    android_lint.lint
  end
  
  message "View the SonarQube report for this build at: https://sonar.intrepid.digital.accenture.com/dashboard?id=Centurylink%3Aandroid"
  