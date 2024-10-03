import React, { useState, useEffect, DeviceEventEmitter} from "react";
import { View, Text, Image, TextInput, StyleSheet, Button, NativeModules, Alert, NativeEventEmitter } from 'react-native';


const { Client } = NativeModules;


// const socket = io('localhost:9999');

// socket.on('connect', () => {
//   console.log('Connected to Socket.io server');
// });

// socket.on('disconnect', () => {
//   console.log('Disconnected from Socket.io server');
// });

const App = () => {

  //const [message, setMessage] = useState('');

  const eventEmitter = new NativeEventEmitter(NativeModules.MyModule);

  const [word, setWord] = useState('');

  const [blank, setBlank] = useState('');

  const [picture, setPicture] = useState(require('./picture8.png'));  

  const [gameLost, setLost] = useState(0);

  const [isButtonVisible, setVisibleButton] = useState(false);

  const [showConnectionbutton, setConnectionbutton] = useState(true);

  const [false1, setFalse1] = useState('');
  const [false2, setFalse2] = useState('');
  const [false3, setFalse3] = useState('');
  const [false4, setFalse4] = useState('');
  const [false5, setFalse5] = useState('');
  const [false6, setFalse6] = useState('');
  const [false7, setFalse7] = useState('');

  /*const handleKeyboardButton = (value) => {
    console.log(value)
    alert(value);
   // Client.guess(value);
  };*/

  async function handleKeyboardButton(value){
    const success = await Client.sendMessage(value);
    //alert(success);
  }

  async function SwitchButtonVisibility(value){
    setVisibleButton(value);
  }
  
  async function hideConnectButton(){
    setConnectionbutton(false);
  }

  async function connectButton(){
    const success = await Client.connectToServer();
    if(success == 1){
      alert("Connected!");
      hideConnectButton();
      SwitchButtonVisibility(true);
    }  
    else alert("Connection failed!")
  }

  async function startButton(){
    //restartGame();
    const success = await Client.sendMessage("/start");
    //SwitchButtonVisibility(false);
  } 
  

  
  eventEmitter.addListener("myEvent", (event) => {
    const split = event.split(",");
    console.log(split)
    setGameState(split);
  });

  async function setGameState(array){
    
    if(array[0] == "/start"){
      restartGame();
      SwitchButtonVisibility(false);
      return;
    }
    
    if(array[0] != undefined) setWord(array[0]); 
    if(array[1] != undefined) setBlank(array[1]);
    if(array[2] != undefined) setFalse1(array[2]); 
    if(array[3] != undefined) setFalse2(array[3]);
    if(array[4] != undefined) setFalse3(array[4]);
    if(array[5] != undefined) setFalse4(array[5]);
    if(array[6] != undefined) setFalse5(array[6]);
    if(array[7] != undefined) setFalse6(array[7]);
    if(array[8] != undefined) setFalse7(array[8]);
    
    
    
    //pictures:
    
    if(array[9] != undefined) {
      setLost(1);
      setPicture(require('./picture8.png'));
      setBlank("You lost! The word was " + word)
      SwitchButtonVisibility(true);
      return;
    }
    
      if(array[8] != undefined) setPicture(require('./picture7.png'));
      else if(array[7] != undefined) setPicture(require('./picture6.png'));
      else if(array[6] != undefined) setPicture(require('./picture5.png'));
      else if(array[5] != undefined) setPicture(require('./picture4.png'));
      else if(array[4] != undefined) setPicture(require('./picture3.png'));
      else if(array[3] != undefined){
        setPicture(require('./picture2.png'));
        console.log("should show second picture2")
      } 
      else if((array[2] != undefined) && array[2] != ""){
        setPicture(require('./picture1.png'));
        console.log("should show first picture")
      } 
        //else setPicture(require('./picture0.png'));
    
    console.log(blank)
    if((array[1] == word) && word != ''){
      setBlank("You won! You guessed the word " + word);
      SwitchButtonVisibility(true);
    }
  }


  async function restartGame(array){
    setPicture(require('./picture0.png'))
    setWord(""); 
    setBlank("");
    setFalse1(""); 
    setFalse2("");
    setFalse3("");
    setFalse4("");
    setFalse5("");
    setFalse6("");
    setFalse7("");
    setFalse7("");  
    setLost(0);  
  }
  return (
    <View style = {[styles.container, {flexDirection: "column"}]}>
      <View>
        {showConnectionbutton && (<Button title = {"connect to server"} onPress={connectButton}/>)}
        {isButtonVisible && (<Button onPress={startButton} title = {"start"}/>)}
      </View>
      <View style = {styles.row}>
        <View style = {{ flex: 1}}>   
          <View style = {{flex: 1}}>
            <Text style = {[ styles.falseletters]}>{false1}</Text>
            <Text style = {[ styles.falseletters]}>{false2}</Text>
            <Text style = {[ styles.falseletters]}>{false3}</Text>
          </View>
        </View>
        <Image resizeMode='contain' style = {{flex: 6}}  source={ picture }/>
        <View style = {{ flex: 1}}>   
          <View style = {{flex: 1}}>
            <Text style = {[ styles.falseletters]}>{false4}</Text>
            <Text style = {[ styles.falseletters]}>{false5}</Text>
            <Text style = {[ styles.falseletters]}>{false6}</Text>
            <Text style = {[ styles.falseletters]}>{false7}</Text>
          </View>
        </View>
      </View>
      <View style = {{ flex: 1}}>
        <Text style = {[styles.blank]}>{blank}</Text>
      </View>
      <View style = {{flex: 2, flexDirection: "column"}}>
        <View style = {{flex: 1, flexDirection: "row"}}>
          <View style = {[styles.buttonContainer]}>
              <Button  title={"Q"}  style = {[styles.button]} onPress={() => handleKeyboardButton('Q')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"W"}  style = {[styles.button]} onPress={() => handleKeyboardButton('W')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"E"}  style = {[styles.button]} onPress={() => handleKeyboardButton('E')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"R"}  style = {[styles.button]} onPress={() => handleKeyboardButton('R')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"T"}  style = {[styles.button]} onPress={() => handleKeyboardButton('T')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"Y"}  style = {[styles.button]} onPress={() => handleKeyboardButton('Y')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"Z"}  style = {[styles.button]} onPress={() => handleKeyboardButton('Z')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"U"}  style = {[styles.button]} onPress={() => handleKeyboardButton('U')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"I"}  style = {[styles.button]} onPress={() => handleKeyboardButton('I')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"O"}  style = {[styles.button]} onPress={() => handleKeyboardButton('O')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"P"}  style = {[styles.button]} onPress={() => handleKeyboardButton('P')}/>
            </View>
            
          </View>
          <View style = {{flex: 1, flexDirection: "row"}}>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"A"}  style = {[styles.button]} onPress={() => handleKeyboardButton('A')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"S"}  style = {[styles.button]} onPress={() => handleKeyboardButton('S')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"D"}  style = {[styles.button]} onPress={() => handleKeyboardButton('D')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"F"}  style = {[styles.button]} onPress={() => handleKeyboardButton('F')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"G"}  style = {[styles.button]} onPress={() => handleKeyboardButton('G')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"H"}  style = {[styles.button]} onPress={() => handleKeyboardButton('H')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"J"}  style = {[styles.button]} onPress={() => handleKeyboardButton('J')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"K"}  style = {[styles.button]} onPress={() => handleKeyboardButton('K')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"L"}  style = {[styles.button]} onPress={() => handleKeyboardButton('L')}/>
            </View>
          </View>
          <View style = {{flex: 1, flexDirection: "row"}}>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"Z"}  style = {[styles.button]} onPress={() => handleKeyboardButton('Z')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"X"}  style = {[styles.button]} onPress={() => handleKeyboardButton('X')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"C"}  style = {[styles.button]} onPress={() => handleKeyboardButton('C')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"V"}  style = {[styles.button]} onPress={() => handleKeyboardButton('V')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"B"}  style = {[styles.button]} onPress={() => handleKeyboardButton('B')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"N"}  style = {[styles.button]} onPress={() => handleKeyboardButton('N')}/>
            </View>
            <View style = {[styles.buttonContainer]}>
              <Button  title={"M"}  style = {[styles.button]} onPress={() => handleKeyboardButton('M')}/>
            </View>
          </View>
        </View>
      </View>
  );
}


const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 10
  },
  row: {
    flexDirection: "row",
    flex: 5
  },
  falseletters:{
    flex: 1,
    textAlign: "center",
    fontSize: 45,
    marginTop: 15,
    color: "darkred",
    fontFamily: "notoserif",
    textDecorationLine: 'line-through'
  },

  blank: {
    textAlign: "center",
    fontSize: 30,
    fontFamily: "notoserif",
    color: "black"
  },

  buttonContainer: {
    flex: 1,
    margin: 3
  }
});



export default App;