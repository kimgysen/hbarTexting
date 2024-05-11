//SPDX-License-Identifier: UNLICENSED
pragma solidity >=0.7.0 <0.8.9;

contract Suppliers {
    
    struct supplier_info{
        int supplier_id;
        address supplier_address;
        string email_address;
    }


    supplier_info[] suppliers;


    int supplierCounter;
    address payable client;

    modifier onlyClient() {
        require(msg.sender == client, "Only client can call this!");
        _;
    }


    constructor() {
        client = payable(msg.sender);
        supplierCounter = 0;
    }


    function addSupplier(address supplier_address_, string memory email_address_) public onlyClient{
        require(!validateSupplier(email_address_), "Supplier already registered!");
        suppliers.push(supplier_info(supplierCounter++, supplier_address_, email_address_));
    }


    function validateSupplier(string memory email_address_) public view onlyClient returns(bool){
        bool issupplier= false;
        for(uint i=0;i<suppliers.length;i++){
            if(keccak256(bytes(suppliers[i].email_address)) == keccak256(bytes(email_address_))){
                issupplier = true;
                break;
            }
        }  
        return issupplier;
    }

}