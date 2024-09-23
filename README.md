# Catapult
<p>
  Cat app for android phone that list all (the cutest) races of cats and all information about them. <br/>
  You can also take a quiz to see how well you know cats and publish it to leaderboard.
</p>

## Getting Started
<h4><ins>Requirements:</ins></h4>
<p>
  
  - Kotlin 1.9.22
  - Gradle 8.3.0 or above
  - Jetpack Compose 1.5.8 or above
</p>

#### <ins>Backend setup:</ins>
<p>

  First you should get your cat api key, go to this link: https://thecatapi.com/
  
  ![caat](https://github.com/user-attachments/assets/1fc23e08-9330-41a6-ad54-fbf46dcf5169)
  
  After you have gotten you api key, open project and inside root directory `Catapult` create file `keystore.properties`, so full directory should be `Catapult/keystore.properties` with structure:

  ```kotlin
CAT_API_KEY= #your_api_key
```

You can run `MainActivity.kt` after and project will start.
</p>

<h2> Project description </h2>
<p>
  There are three main parts.
</p>
<p>  
  
  **First**: Person can see all information about cats, including a gallery of pictures for every breed.
</p>
<p>
  
  **Second**: There is a quiz system, person can choose between three types of quizzes about cats, to see how well they know them. 
  They are "Guess the cat", "Guess the fact", "Left or right cat". After finishing the quiz, person gets a score of how well they did.
  They can then publish that to leaderboard which brings us to third part.
</p>
<p>
  
  **Third**: User can login with account and see their quiz result history. They can also publish their result to leaderboard to see how well they did compared to others.
</p>

## Features & Technologies 
#### <ins>Backend:</ins>
<p>
  
  - Kotlin
  - Gradle
  - Json
  - Room
  - Hilt
  - DataStore
  - Jetpack Navigation
  - Retrofit
  - OkHttp
  - Coil
</p> 
