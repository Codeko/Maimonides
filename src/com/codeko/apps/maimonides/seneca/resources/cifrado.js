//Funciones para el encriptado por el algoritmo RSA
function cifrar(cadena){
 var cadascii, i,j,k, cadcif,caddescif;
 cadascii = "";
 for (i = 0; i < cadena.length; i++){
  cadascii = cadascii + cadena.charCodeAt(i);
 }
 cadcif = criptar(cadascii);
 return cadcif;
}


function criptar(base){
  var x,r,i,z;
  x = base.toString();
  i = '3584956249';
  z = '356056806984207294102423357623243547284021';
  r = '1';
  while (i != '0'){
    if (impar(i)){ r = modulo(mulgrande(r,x) , z); }
    x = modulo(mulgrande(x,x) , z);
    i = divpordos(i);
  }
  return r;
}

function modulo(a,z){
  var nuevo_a,k,t,b,n_m;
  nuevo_a = a.toString();z = z.toString();
  k = Math.min(z.length,14) ;
  b = (parseInt(z.substr(0,k),10) + 1);
  while (mayorque(nuevo_a , z) == 1){
    t = parseInt(nuevo_a.substr(0,k),10) / b;
	n_m = nuevo_a.length - z.length;
	if (t < 1 && n_m ==0){
	  nuevo_a = resta(nuevo_a,z);
	}
	else{
	  t = desplazacoma(t.toString(),n_m);
	  nuevo_a = resta(nuevo_a, mulgrande(z,t.toString()));
	}
  }
  if (mayorque(nuevo_a , z) == 2) {return nuevo_a;}
  if (nuevo_a == z) {return '0';}
  return -1; 
}


function resta(num1,num2){
var acarreo, i, solucion, minuendo, sustraendo;
   num1 = qcerosi(num1);
   num2 = qcerosi(num2);
	 if (num1.length < num2.length){
		aux = num2;
		num2 = num1;
		num1 = aux;
	 }
	 while (num2.length < num1.length) {num2 = "0" + num2;}
	 if (num1 == num2){return '0';}
	 else {
	   if (num1.length < 14) {return (parseInt(num1,10) - parseInt(num2,10)).toString(); }
	 }
	 solucion = ""; 
	 acarreo = 0;
	 for (i = num1.length - 1; i >= 0 ; i--){ 
	   minuendo = parseInt(num1.substr(i,1),10) ;
	   sustraendo = parseInt(num2.substr(i,1),10) + acarreo;
	   if(minuendo < sustraendo ){minuendo = minuendo + 10; acarreo = 1;}
       else {acarreo = 0;}
	   solucion = (minuendo - sustraendo).toString() + solucion;
	 }
	
	solucion = qcerosi(solucion);
    return solucion;
}


function mayorque(num1,num2){
 if (num1.length > num2.length){   
	   while(num2.length < num1.length){
		 num2 = '0' + num2;
	   }   
 }
 if (num2.length > num1.length){  
	   while(num1.length < num2.length){
		 num1 = '0' + num1;
	   }   
 }
 if (num1 > num2){ return 1;}
 if (num1 < num2){ return 2;}
 if (num1 == num2){ return 0;}
}

function desplazacoma(num,posi){
var p,mant,dec;
 p = num.indexOf('.');
 if (p == -1){
   dec = num;
   mant = "";
  }
 else {
   mant = num.substr(0,p);
   dec = num.substr(p +1);
 }
   if (dec.length <= posi){ while (dec.length < posi){dec = dec + '0';}}
   else {dec = dec.substr(0,posi);}
 mant = mant + dec;
 return qcerosi(mant);
}


function divpordos(num){
  var solucion;
  solucion = mulgrande(num.toString(),'5');
  if (solucion.length > 1){ solucion = solucion.substr(0,solucion.length - 1);}
  else { solucion = '0';}
  return solucion;
}
 
function impar(num){
 if(num.substr(num.length -1)=='1' || num.substr(num.length -1)=='3' || num.substr(num.length -1)=='5' || num.substr(num.length -1)=='7' || num.substr(num.length -1)=='9'){
   return true;}
 else {return false;}
}

function mulgrande(num1,num2){
 var factor1,factor2,partes,i,j,solucion,aux,acarreo;
 if (num1.length + num2.length <= 14){ return (parseInt(num1,10)* parseInt(num2,10)).toString(); }
 if (num2.length == 1) { return mulunacifra(num1,num2);}

  factor1 = new Array();
  factor2 = new Array();
  partes = new Array();
  i = 7; j = 0;
  while ( i < num1.length){  
    factor1[j] = num1.substr(num1.length - i ,7);
	i += 7;
	j += 1;
  }
  if (i != num1.length -1){
   factor1[j] = num1.substr(0,num1.length + 7 - i);
  }
 i = 7; j = 0;
  while ( i < num2.length){  
    factor2[j] = num2.substr(num2.length - i ,7);
	i += 7;
	j += 1;
  }
  if (i != num2.length -1){
   factor2[j] = num2.substr(0,num2.length + 7 - i);
  }

  for (i = 0; i < factor1.length; i++) {
     for (j = 0; j < factor2.length;  j++) {
	   if (partes[j + i] == null){ partes[j + i] = multiplica(factor1[i],factor2[j]).toString();}
	   else {partes[j + i] = (parseInt(partes[j + i],10) + parseInt(multiplica(factor1[i],factor2[j]),10)).toString();}
	   while(partes[j + i].length <= 7){ partes[j + i] = '0' + partes[j + i];} 
	 }  
  }

  solucion = partes[0].substr(partes[0].length - 7); // final de la cadena
  for (i = 0; i < partes.length - 1; i++) {
    aux = ( parseInt(partes[i].substr(0,partes[i].length - 7),10) + parseInt(partes[i + 1],10) ).toString();
    while(aux.length <= 7){ aux = '0' + aux;} 
	partes[i + 1] = aux;
	if (aux.length > 7 && i < partes.length -2){aux = aux.substr(aux.length - 7);} 
    solucion = aux + solucion;
  }
  if (partes.length == 1){solucion = partes[0];}
  solucion = qcerosi(solucion);
  return solucion;
}


function multiplica(num1,num2){
 var  lon, alfa,beta, ganma,aux, solucion;
 if (num1.length + num2.length <= 14){ return (parseInt(num1,10)* parseInt(num2,10)).toString(); }
 
 if (num1.length < num2.length){
    aux = num2;
	num2 = num1;
	num1 = aux;
 }
 while (num2.length < num1.length) {num2 = "0" + num2;}
 
 solucion = "";
 a = num1.substr(0,parseInt(num1.length / 2) );
 b = num1.substr(parseInt(num1.length / 2) );
 c = num2.substr(0,parseInt(num2.length / 2) );
 d = num2.substr(parseInt(num2.length / 2) );
 
 lon = b.length; 
 
 alfa = parseInt(a,10) * parseInt(c,10);
 beta =  (parseInt(b,10) * parseInt(d,10)).toString();
 ganma = parseInt(a,10) * parseInt(d,10) + parseInt(b,10) * parseInt(c,10); 

 while (beta.length <= lon) { beta = '0' + beta;}
 solucion = solucion + beta.substr(beta.length - lon);
 aux = (parseInt(beta.substr(0, beta.length - lon),10) + ganma ).toString();
 while (aux.length <= lon) { aux = '0' + aux;}
 solucion = (parseInt(aux.substr(0, aux.length - lon),10) + alfa).toString() + aux.substr(aux.length - lon) + solucion;
 return solucion;
}

function mulunacifra(num1,cifra){
	var acarreo,solucion,i,prod,r;
	if (cifra == '0') {return '0';}
	if (cifra == '1') {return num1.toString();}
	solucion = '';
	acarreo = 0;
	for (i = num1.length - 1; i >= 0 ; i--){	  
	  prod = parseInt(num1.substr(i,1),10) * parseInt(cifra,10) + acarreo;
	  r = prod.toString();
	  if (prod > 9){ acarreo = parseInt(r.substr(0,1),10); r = r.substr(1,1); }
      else { acarreo = 0;}
      solucion = r + solucion;
	}
	if (acarreo > 0){ solucion = acarreo.toString() + solucion;}
	solucion = qcerosi(solucion);
	return solucion;
}

function qcerosi(num){
var i;
  i = 0;
  while (i < num.length && num.charAt(i) == '0'){ i++;} 
  if (i == num.length ){ num = '0';}
  else {num = num.substr(i);} 
  return num;
}