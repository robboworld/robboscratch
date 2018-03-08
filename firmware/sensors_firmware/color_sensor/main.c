#include <avr/io.h>
#include <stdio.h>
#include <stdlib.h>
#include <util/delay.h>
#include <avr/interrupt.h>


#define RED_CHANAL_PIN   PA2 
#define GREEN_CHANAL_PIN PA1
#define BLUE_CHANAL_PIN  PA0
//#define GAIN_PIN         PA0
#define OUTPUT_PIN       PA3
#define FILTER 4

uint8_t   que_size=9;

//uint8_t CurrentFilter = 0;
//uint8_t ColorValue[3][FILTER];
//uint8_t CurrentValue[3];
uint8_t CurrentColor = 0;
//_Bool ColorComplete = 0;

void ADC_config(void);

//uint16_t temp=0;
uint8_t i = 0;
uint8_t j=0;
//uint8_t c;

//uint8_t red;
//uint8_t green;
//uint8_t blue;
uint8_t t1;
uint16_t t3,t2;
//uint8_t DeltaHalfColor;
//char toggle = 0;
uint8_t arr_post[5]={0,255,1,1,1};  //170 (AA) for debug
uint8_t post=255;
//uint8_t tmpclr[3];

// uint8_t que_index = 0;


uint8_t   red_que[] =  {1,1,1,1,1,1,1,1,1};
uint8_t   green_que[] ={1,1,1,1,1,1,1,1,1};
uint8_t   blue_que[] = {1,1,1,1,1,1,1,1,1};

//uint16_t   red_que[] =  {1,1,1,1,1,1,1,1,1};
//uint16_t   green_que[] ={1,1,1,1,1,1,1,1,1};
//uint16_t   blue_que[] = {1,1,1,1,1,1,1,1,1};



//int   red_que[] =  {1,1,1,1,1};
//int   green_que[] ={1,1,1,1,1};
//int   blue_que[] = {1,1,1,1,1};

uint8_t color_buf[] = {1,1,1};


int median_index = 4;

//uint16_t high_bit_signal_flag = 0;



 void bubleSort( uint8_t *arr, uint8_t *mas, int size) {
   
 int k = 0;
  for (k = 0; k< size; k++ )
          {

              
             mas[k] = arr[k];
                 

        }

    int i = 0;       
   for(i = 0; i < size; i++) {
       // interruptFlag = true;
	int j = 0;
        for(j = 0; j < size - i - 1; j++) {
            if(mas[j] > mas[j+1]) {
                uint8_t tmp = mas[j];
                mas[j] = mas[j+1];
                mas[j+1] = tmp;
                //Была хотя бы одна замена элементов => нужен еще проход по i
             //   interruptFlag = false;
            }
        }
 
        //Если не было замен, то заканиваем проходы
      /*  if(interruptFlag) {
          
            return mas;
            break;
        }*/
    }



}


void arr_left_shift( uint8_t *arr,int arr_size){

	int i = 0;
        for (i = 0; i < (arr_size-1); i++)

            {
                
                arr[i] = arr[i+1];


            }  

                arr[arr_size-1] = 0;


}

 uint8_t median_filter(uint8_t* que, uint8_t new_que_element, int median_index, int size)
{
    
    uint8_t buf[9];  

   

       bubleSort(que,buf,size);


      /*  for (int i = 0; i< que_size; i++ )
            {

              
                printf("%d | ",buf[i]);
                 

        }

            printf("\n\n"); */

        
        arr_left_shift(que,size);

        
     

     

        que[size-1] = new_que_element;

  /*for (int i = 0; i< que_size; i++ )
            {

              
                printf("%d | ",que[i]);
                 

        }

            printf("\n\n");*/


        

        return buf[median_index];   

     
      


}


int main(void)
{
    //CLKPR = 0x80; 
    //CLKPR = 0x00; // установка предделителя частоты в 1
    DDRA |= (1 << OUTPUT_PIN) ; //Настраиваем пины на выход
    ADC_config();
   // sei();

TCCR0B = (0<<CS02)|(1<<CS01)|(0<<CS00);
TIFR0 = 1<<TOV0;
TCCR0A = 0x02;
  // OCR0A = 100; // Count 100 cycles for interrupt
  // OCR0A = 50; // Count 50 cycles for interrupt
 // OCR0A = 0x70; // Count 10 cycles for interrupt
TCNT0 = 0;
  TIMSK0 |= 1<<TOIE0; // enable timer compare interrupt

    
    sei();              // global interrupt enable
	while (1)
	{
		//for(CurrentFilter=0;CurrentFilter<FILTER;CurrentFilter++)
		//{

			//high_bit_signal_flag=0; //reset to 0

			for(CurrentColor=0;CurrentColor<3;CurrentColor++)
			{	
				ADMUX = (ADMUX & 0xF0) | (CurrentColor);
				//_delay_ms(3);  //delay    beetween  colors  read
				ADCSRA |= ( 1 << ADSC ); //Запускаем АЦП
				while (ADCSRA & (1 << ADSC) );{ // wait till conversion complete
					asm volatile("NOP");
				}
				//ColorValue[CurrentColor][CurrentFilter]=ADCH;
				t1=ADCL;
				t2=ADCH;
				//t3=(t2<<8)+t1;
                if (t2) t3=254;
                else t3 = t1;				
                //scale 10bit to 8bit

				//t3=t3>>2; 
				
				/* if ((t3&0x300)){ //check 9th bit
					
					//t3=t3>>2; 
                       t3 = 254; 
					

					//high_bit_signal_flag = 2;

				} */

				//high_bit_signal_flag|= (t3&0x300);
				
					
				//t3 = t3&0xFF;


				//if( t3 >= 254) t3= 254;
				//if( t3 == 0) t3=1;
			//	arr_post[2+2-CurrentColor]=(uint8_t)t3; //uncomment to restore normal mode  //write  170 (AA)  to debug
				//arr_post[2+CurrentColor]+=ADCH;
				
				if (CurrentColor ==2)  
					{
							
					//color_buf[CurrentColor] = median_filter(red_que,t3,median_index,que_size);
                    //median_filter(red_que,t3,median_index,que_size);
					color_buf[0] =  median_filter(red_que,(uint8_t)t3,median_index,que_size);

                      //  color_buf[0] = t3;

					}

				if (CurrentColor == 1)
					{
							
					//color_buf[CurrentColor] = median_filter(green_que,t3,median_index,que_size);
                   // median_filter(green_que,t3,median_index,que_size);
					color_buf[1] = median_filter(green_que,(uint8_t)t3,median_index,que_size);

                       //  color_buf[1] = t3;
	
					}

				if (CurrentColor == 0)
					{

						
					// color_buf[CurrentColor] =    median_filter(blue_que,t3,median_index,que_size);

                   // median_filter(blue_que,t3,median_index,que_size);
					color_buf[2]  = median_filter(blue_que,(uint8_t)t3,median_index,que_size);

                        //  color_buf[2] = t3;

					}	
				
			}//end of for

		
			cli();//disable interrupts;


            
           
            

            arr_post[2] = (color_buf[0] == 0)?1:color_buf[0];
            arr_post[2] = (color_buf[0] == 255)?254:color_buf[0];

              arr_post[3] = (color_buf[1] == 0)?1:color_buf[1];
             arr_post[3] = (color_buf[1] == 255)?254:color_buf[1];


              arr_post[4] = (color_buf[2] == 0)?1:color_buf[2];
              arr_post[4] = (color_buf[2] == 255)?254:color_buf[2]; 

            
             //    arr_post[2] = 170;
             //    arr_post[3] = 170;
             //    arr_post[4] = 170;   

            

			//high_bit_signal_flag = high_bit_signal_flag>>8;
			
			//if (high_bit_signal_flag >= 0x300) high_bit_signal_flag = 2;
			//if (high_bit_signal_flag >= 0x200) high_bit_signal_flag = 1;
			
			// color_buf[0] >>=high_bit_signal_flag;

			//color_buf[0] = color_buf[0] >> high_bit_signal_flag;
				
			//arr_post[2]=( color_buf[0] >= 254)?254:(uint8_t)color_buf[0];  
			//	 arr_post[2]=( color_buf[0])?(uint8_t)color_buf[0]:1;	


				
	
			 //color_buf[1] >>= high_bit_signal_flag;
			// color_buf[1] = color_buf[1] >> high_bit_signal_flag;

			//arr_post[3]=( color_buf[1] >= 254)?254:(uint8_t)color_buf[1];  
			//	 arr_post[3]=( color_buf[1])?(uint8_t)color_buf[1]:1;

				// arr_post[3] = 114;
				
			// color_buf[2] >>= high_bit_signal_flag;
			//color_buf[2] = color_buf[2] >> high_bit_signal_flag;	

					
			//arr_post[4]=( color_buf[2] >= 254)?254:(uint8_t)color_buf[2];  
			//	arr_post[4]=( color_buf[2])?(uint8_t)color_buf[2]:1;

			//	arr_post[4] = 114;

			
			sei(); //enable interrupts

			

		

			

    //  que_index++;


		//}
/*
		for(CurrentColor=0;CurrentColor<3;CurrentColor++)
		{
				
			for(CurrentFilter=0;CurrentFilter<FILTER;CurrentFilter++)
			{
				tmpclr[CurrentColor]+=ColorValue[CurrentColor][CurrentFilter];
			}
			tmpclr[CurrentColor]/=FILTER;
		}
		for(CurrentColor=0;CurrentColor<3;CurrentColor++)
		{
			arr_post[2+CurrentColor]=tmpclr[CurrentColor];
		}
*/	



 /*  for (t=2; t<= 4; t++)
{
   arr_post[t] = test_value;
   
}
      test_value++; */

      


	}
   

}
ISR (TIM0_OVF_vect)
{
	TCNT0 = 245; //adjust coeff 245 - 115.8us
  
	if(post & (1<<j)) PORTA |= (1 << OUTPUT_PIN);
	else PORTA &= ~(1 << OUTPUT_PIN);

	j++;
	if(j==8){

		j=0;
		i++;
		if(i>4) i=0;

    
		post=arr_post[i];

        


	}


}

void ADC_config(void){
    ADMUX =
            (0 << ADLAR) |     // left shift result
            (1 << REFS1) |     // Sets ref. voltage to VCC, bit 1
            (0 << REFS0) |     // Sets ref. voltage to VCC, bit 0
            //Выставление этих битов определяет рабочий канал АЦП
            (0 << MUX3)  |     // use ADC2 for input (PB4), MUX bit 3 
            (0 << MUX2)  |     // use ADC2 for input (PB4), MUX bit 2
            (0 << MUX1)  |     // use ADC2 for input (PB4), MUX bit 1
            (0 << MUX0);       // use ADC2 for input (PB4), MUX bit 0
    ADCSRA = 
            (1 << ADEN)  |     // Enable ADC
            //(0 << ADIE)  |     // Включаем прерывания АЦП
                               // Настройка коэффициентов масштабирования. 1/32
            (1 << ADPS2) |     // set prescaler to 128, bit 2 
            (0 << ADPS1) |     // set prescaler to 128, bit 1 
            (1 << ADPS0);      // set prescaler to 128, bit 0      
}


//Обработчик прерывания. Вызывается по завершению оцифровки.
/*
ISR(ADC_vect)
{
    
    ColorValue[CurrentColor][CurrentFilter] = ADCH; //Читаем оцифрованное значение
    CurrentColor++;
    if( CurrentColor >= 3 ) //Если прочитали все цвета
    {
        CurrentColor = 0; // - начинаем сначала
        ColorComplete = 1; //Поднимаем флага окончания оцифровки всех каналов
        CurrentFilter++;
        if(CurrentFilter >= FILTER)
        {
            CurrentFilter = 0;
        }
    }
    ADMUX = (ADMUX & 0xF0) | (CurrentColor + 1); // Выбираем новый канал для оцифровки
    //PORTB ^= (1 << OUTPUT_PIN);
    ADCSRA |= ( 1 << ADSC ); // Запускаем АЦП
    
}
*/

//avr-gcc -Wall -Os -DF_CPU=8000000UL -mmcu=attiny85 -o main.o main.c
//avr-objcopy -j .text -j .data -O ihex main.o main.hex

