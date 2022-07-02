db.HBARquotes.drop();
db.HBARquotes.insertMany(
[
  {
    timestamp: '2022-06-24 13:14:17.609',
    HBAR_USD: 0.07110
  },
  {
    timestamp: '2022-06-25 13:14:17.609',
    HBAR_USD: 0.07120
  },
  {
    timestamp: '2022-06-26 13:14:17.609',
    HBAR_USD: 0.07130
  }
]);
db.HBARquotes.find();
quit();