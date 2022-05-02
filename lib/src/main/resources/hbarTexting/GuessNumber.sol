//SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.7.0 <0.8.9;

contract GuessNumber {
    // the contract's owner, set in the constructor
    address owner;
    
    // the secret number to guess
    uint secret_number;

    // the messages we're storing
    string message_lower;
    string message_equal;
    string message_higher;


    constructor  (uint secret_number_, string memory message_lower_, string memory message_equal_, string memory message_higher_)  
    {
        // set the owner of the contract for 'kill()'
        owner = msg.sender;
        secret_number = secret_number_;
        message_lower = message_lower_;
        message_equal = message_equal_;
        message_higher = message_higher_;
    }

    function guess(uint number_) public view returns (string memory) {
        // only allow the owner to update the message
        if (number_  < secret_number)  return message_lower;
        if (number_ == secret_number)  return message_equal;
        if (number_  >  secret_number) return message_higher;        
    }


    // recover the funds of the contract
    function kill() public { if (msg.sender == owner) selfdestruct(payable(msg.sender)); }
}