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

#define APWIN 9						// Ðàçìåð àïåðòóðíîãî îêíà ôèëüòðà
#define APWINMAX (APWIN-1)<<2
#define MEDIAN APWIN>>1				//
#define QMASK  0xFC					// Ñòàðøèå ðàçðÿäû äëÿ íóìåðàöèè ýëåìåíòîâ î÷åðåäè 
#define NQMASK 0x3
#define QSHIFT 0x4

//uint8_t   que_size=9;

//uint8_t CurrentFilter = 0;
//uint8_t ColorValue[3][FILTER];
//uint8_t CurrentValue[3];
//uint8_t CurrentColor = 0;
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
uint8_t arr_pos[5]={0,255,170,85,170};  //170 (AA) for debug
uint8_t post=255;


typedef  struct {
	uint8_t ApValHi[APWIN];      
	uint8_t ApValLo[APWIN];      
} RGB_t;

static RGB_t RGB[3];
RGB_t *Crnt;

uint8_t  FrameRGB[3];        // Êàäð RGB äëÿ îòïðàâêè íàðóæó
uint8_t  KRGB[3];
uint8_t  CorrRGB[3];

inline int8_t CompareAp(uint8_t Hi, uint8_t Lo, uint8_t Index) {  //нужно

	register uint8_t H, L;
	
	H = Crnt->ApValHi[Index]&NQMASK;
	// Hi &= NQMASK;
	L = Crnt->ApValLo[Index];
	H=  (Hi < H)? -1: (Hi > H);
	if  (!H)  H = (Lo < L) ? -1 : (Lo > L);
	return H;
	}

inline void initAP() {  //нужно в стартапе
	register uint8_t i,j,k;
	j =APWINMAX ;
	Crnt = &RGB[0];    //указатель на структуру 
   for (k=0; k^3; k++)
	for (i = 0; i < APWIN; i++) {
		Crnt[k].ApValHi[i] = j;
		j -= QSHIFT;
       	Crnt[k].ApValLo[i] = 0;
	}
}



inline void AutoL() {  //запускать после фильра
	register uint8_t i,Lo;
	register uint16_t s;

	
	Lo ^= Lo;
	for (i^=i; i^3; i++) 
	  Lo |= RGB[i].ApValHi[MEDIAN];

	Lo = (Lo & 2) ? 2 : Lo & 1;
	// cli(); //возможно включить
		
	for (i ^= i; i ^ 0x3; i++) {
		//*((uint8_t *)&s + 1) = RGB[i].ApValHi[MEDIAN];
		//*(uint8_t *) &s = RGB[i].ApValLo[MEDIAN];

        s = (RGB[i].ApValHi[MEDIAN] << 8) + RGB[i].ApValLo[MEDIAN];
		s >>= Lo;
		FrameRGB[i] = s&0xFF;  //отдаю в канал
	}


	// sei(); //возможно включить
	}
	






inline void Median8(uint8_t Hi, uint8_t Lo) {

	int8_t  j=0, i, Fl;
   
    

	Hi &= NQMASK;
	for (i = 0; i < APWIN; i++)
		if (Crnt->ApValHi[i] & QMASK) Crnt->ApValHi[i] -= QSHIFT;   // äåêðåìåíò íîìåðà ýëåìåíòà â î÷åðåäè íà âûõîä    //Crnt перед вызовом median смотреть на канал, который фильтруем
		else  j = i;							// îäíîâðåìåííî èùåì óäàëÿåìûé ýëåìåíò

			
    Fl = CompareAp(Hi, Lo, j);

	if (Fl) {			// Ñðàâíèâàåì íîâûé ýëåìåíò ñ óäàëÿåìûì

			i = (Fl < 0) ? 0 : APWIN - 1;

			for (; j^i; j += Fl) {

				if (Fl == CompareAp(Hi, Lo, j + Fl)) {
					Crnt->ApValHi[j] = Crnt->ApValHi[j + Fl];
					Crnt->ApValLo[j] = Crnt->ApValLo[j + Fl];
				}
				else break;
			}
		}
	
	Crnt->ApValHi[j] = Hi|APWINMAX;
	Crnt->ApValLo[j] = Lo;

	
}


int main(void)
{

  uint8_t CurrentColor = 0;


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

    initAP(); //median filter init

	while (1)
	{
		//for(CurrentFilter=0;CurrentFilter<FILTER;CurrentFilter++)
		//{

			//high_bit_signal_flag=0; //reset to 0

			for(CurrentColor=0;CurrentColor < 3;CurrentColor++)
			{	
				ADMUX = (ADMUX & 0xF0) | (CurrentColor);
				//_delay_ms(3);  //delay    beetween  colors  read
				ADCSRA |= ( 1 << ADSC ); //Запускаем АЦП
				while (ADCSRA & (1 << ADSC) );{ // wait till conversion complete
					asm volatile("NOP");
				}
				//ColorValue[CurrentColor][CurrentFilter]=ADCH;
				t1=ADCL;
				t2=ADCH&0x3;
				 
            /*    if (t2){
                     t2  = 0;
                     t1 = 254;
   
                 } 
*/
				
                Crnt = &RGB[2-CurrentColor];



				 Median8(t2,t1);
				
			}//end of for


            AutoL();

		
            FrameRGB[0] = (FrameRGB[0] == 0)?1:FrameRGB[0];
            FrameRGB[0] = (FrameRGB[0] == 255)?254:FrameRGB[0];

              FrameRGB[1] = (FrameRGB[1] == 0)?1:FrameRGB[1];
             FrameRGB[1] = (FrameRGB[1] == 255)?254:FrameRGB[1];


              FrameRGB[2] = (FrameRGB[2] == 0)?1:FrameRGB[2];
              FrameRGB[2] = (FrameRGB[2] == 255)?254:FrameRGB[2];
			cli();//disable interrupts;

            arr_post[2] = FrameRGB[0];
            arr_post[3] = FrameRGB[1];
	    arr_post[4] = FrameRGB[2];
               			
			sei(); //enable interrupts

			

		

			

    

      


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
//uint8_t q = 0;
//for(q=0;q<15;q++)
//		asm volatile("NOP");

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