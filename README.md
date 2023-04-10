# reactive-spring-boot
Goal of this project is to unlearn the impertive style of coding in favor of declarative style.
And most importanly explore reactive stack of Spring. Reactive applications are taking down the internet down like storm. Although I am pretty late at joining the wagon, but better late than never.

As a medium of acheving the goal I am developing an Online messeging platform, more like Slack.

Application architecture looks pretty similar to industry standard.

1) A react based ui to post and receive messages.
2) A microservice serving authentication/authorization using REST api (reactive spring boot)
3) A microservice serving as backbone posting and receving message using REST api (reactive spring boot)
4) A Mongodb database to store users and their messages
5) Kafka message broker to deliver updates realtime to different components of the system
6) A microservice to index messages to solr
![image](https://user-images.githubusercontent.com/8924255/230960486-53c08fa2-9cb7-49a5-b7f2-62e72f95cbbb.png)

