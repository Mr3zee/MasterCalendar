# Master Calendar

## Description

This is my second year ITMO University project, that has been done during "Kotlin Client-Server 2020" course, so it is coded entirely on Kotlin programming language, Kotlin/JS for the Client side and Kotlin/JVM  for the Server. 

To run project you need to clone the repository:

`git clone https://gitlab.com/Mr3zee/MasterCalendar.git `

and run Gradle:

`./gradlew :server:run`

#### About App

This is a web calendar service that allows users to create their own calendars of two types:

- Events calendar
- TODO list calendar

##### Events Calendar

This a type of calendar, where you take make notes, schedule events for each day

##### TODO list calendar

This is a type of calendar, where each day is challenge you need to complete and you can mark whether you did it or not

## Current Features

- One *TODO list calendar* with name "Template" and only 2021 year supported
- Database

## Future Features

- ~~Database~~ - **ADDED**
- *Events Calendar*
- Years from 1900 to 9999
- Welcome Page
- Forbidden and Not Found pages
- Notification windows 
- Change names for calendars
- Add and delete more than one calendar
- Combine your calendars into lists, create new ones
- Share your Lists with friends
- See your calendar info on the sidebar
- Select month with a small calendar, not wheel or "up/down" buttons
- Add calendar pictures
- Forgot password button
- Google Auth 
- Remember Me button
- Mobile/Tablet support

## Future Improvements

- Add SSL certificate
- Email confirmation
- Plugin for Gradle to support sass in Kotlin/JS
- Waiting for server response animation
- Make My Lists Hover animation more convenient
- Add server error handler 
- Add login attempts limit
- Check for blank fields in input  
- async server side
- User Information about credentials restrictions
- Invalidate sessions on password/email change
- XSS prevention
- SQL Injection prevention

## Bugs

- Return back button in delete account settings is not blocked during animation 
- Text do not change size depending on length
- Font scale animation is terrible
- Not removing text from inputs on success 
- Different CSRF Tokens for two windows/devices 
- Leap year
- example@mail is valid somehow