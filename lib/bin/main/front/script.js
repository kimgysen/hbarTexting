function handleFile() {
	const fileInput = document.getElementById('fileInput');
	const output 	= document.getElementById('output');
	
	const file = fileInput.files[0];
	if (!file) {
		output.textContent = 'No file selected';
		return;
	} 
	
	const reader = new FileReader();
	
   function drawTree(ctx, x, y, tree, offset)
   {
	if (tree != null)
	{
		ctx.beginPath();
		ctx.arc(x, y, r, 0, 2 * Math.PI);
		ctx.fillStyle = "red";
		ctx.fill();
		ctx.lineWidth = 4;
		ctx.strokeStyle = "blue";
		ctx.stroke();
			
		ctx.font = "10pt Calibri";
		ctx.fillStyle = "black";
		ctx.fillText(JSON.stringify(tree.value, null, 2), x-15, y+5);
		
		if (tree.left != null) {
                newX = x - offset;
                newY = y + 40;
                ctx.moveTo(x, y);
                ctx.lineTo(newX, newY);
                ctx.stroke();
                drawTree(ctx, newX, newY, tree.left, offset / 2);
        }
        
        if (tree.right != null) {
                newX = x + offset;
                newY = y + 40;
                ctx.moveTo(x, y);
                ctx.lineTo(newX, newY);
                ctx.stroke();
                drawTree(ctx, newX, newY, tree.right, offset / 2);
         }
		
	}
   };
	
	reader.onload = function(event) {
		const fileContent = event.target.result;
		try{
			const tree = JSON.parse(fileContent);
			const canvas = document.getElementById("myCanvas");
			const ctx = canvas.getContext("2d");

			var width = canvas.scrollWidth;
			var height = canvas.scrollHeight;
			
			console.log(width+' '+height)
			x = width/2;
			y = 60;
			r = 20;
			
			ctx.clearRect(0, 0, canvas.width, canvas.height);
			drawTree(ctx, x, y, tree, x/2)
			
		} catch(error) {
			output.textContent = 'Error reading JSON file: ' + error.message;
		}
				
	};
	
	reader.readAsText(file);
}