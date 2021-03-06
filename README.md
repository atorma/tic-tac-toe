# Tic-Tac-Toe

A tic-tac-toe game (server + front-end) where you can play against an AI or pit AIs against each other.

You can play tic-tac-toe [here](https://gentle-atoll-1722.herokuapp.com/). Please note that as a free Heroku instance it may be initially slow to respond (restarts after sleep) and has limited memory and CPU resources.

Done
* Game mechanics (state changes etc)
* AIs
  * Random "AI" 
  * Naive AI that just tries to build a long sequence, taking obvious winning and blocking moves
  * Monte Carlo Tree Search AI (multithreaded)
* REST API using Spring MVC and Spring Boot  
* Web user interface
  * Adjustable board size and number of pieces to connect (m,n,k-game) 
  * AngularJS + Angular Material 
  * HTML5 canvas
* Players
  * AI vs AI
  * Human vs AI
  * Human vs human at the same computer
* Heroku deployment
  
Todo 
  * Two human players against each other remotely using Websockets