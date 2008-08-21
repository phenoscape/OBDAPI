all: clean w2r

clean:
	rm default*rb

w2r: obd.wsdl
	wsdl2ruby.rb --wsdl obd.wsdl --type client

obd.wsdl:
	wget http://localhost:8080/axis2/services/OBDQueryService?wsdl -O obd.wsdl

