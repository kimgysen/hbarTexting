//SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.7.0 <0.8.9;

contract GuessNumber {
    // the contract's owner, set in the constructor
    address owner;
    
    // the secret number to guess
    uint private secret_number;

    // the messages we're storing
    string[] message = ["too low...", "Yep!! that is it ;)", "Too hign ..."]; 


    constructor  (uint secret_number_)  
    {
        // set the owner of the contract for 'kill()'
        owner = msg.sender;
        secret_number = secret_number_;
    }

    function guess(uint number_) public view returns (string memory) {
        // only allow the owner to update the message
        string memory ret = "";
        if (number_  < secret_number)  ret =  message[0];
        if (number_ == secret_number)  ret =  message[1];
        if (number_  >  secret_number) ret =  message[2];

        return ret;        
    }


    // recover the funds of the contract
    function kill() public { if (msg.sender == owner) selfdestruct(payable(msg.sender)); }
}