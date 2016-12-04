package com.what.bankws;

import com.rabbitmq.client.AMQP.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.what.models.CustomerResponse;
import com.what.models.Data;
import com.what.models.LoanResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

/**
 *
 * @author Tomoe
 */
@WebService(serviceName = "WhatLoanBrokerService")
public class NewWebService {

    private final String GETBANK_EXCHANGE_NAME = "customer_direct_exchange";
    private final String ROUTING_KEY = "customer";
    private final String EXCHANGE_NAME = "whatLoanBroker";

    @WebMethod(operationName = "getBestOffer")
    public String getBestLoanOffer(@WebParam(name = "ssn") String ssn, @WebParam(name = "loanAmount") double loanAmount, @WebParam(name = "loanDuration") int loanDuration) {
        try {
            String id = UUID.randomUUID().toString();
            String responseToCustomer = "";
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("datdb.cphbusiness.dk");
            factory.setUsername("what");
            factory.setPassword("what");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, EXCHANGE_NAME, "");
            channel.basicConsume(queueName, true, consumer);

            send(ssn, loanAmount, loanDuration, channel, id);
            while (true) {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                BasicProperties properties = delivery.getProperties();
                byte[] body = delivery.getBody();
                if (properties.getCorrelationId().equals(id)) {

                    Map<String, Object> headers = properties.getHeaders();
                    String bankName = headers.get("bankName").toString();
                    String bodyString = removeBom(new String(body));
                    System.out.println("The best offer to you is: ");
                    LoanResponse res = unmarchal(bodyString);
                    String receivedSsn = res.getSsn();
                    if (!receivedSsn.contains("-")) {
                        receivedSsn = receivedSsn.substring(0, 6) + "-" + receivedSsn.substring(6);
                    }
                    CustomerResponse response = new CustomerResponse(receivedSsn, res.getInterestRate(), bankName);
                    responseToCustomer = marshal(response);

                    break;
                }
            }
            return responseToCustomer;

        }
        catch (IOException ex) {
            Logger.getLogger(NewWebService.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (JAXBException ex) {
            Logger.getLogger(NewWebService.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InterruptedException ex) {
            Logger.getLogger(NewWebService.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ShutdownSignalException ex) {
            Logger.getLogger(NewWebService.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (ConsumerCancelledException ex) {
            Logger.getLogger(NewWebService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void send(String ssn, double loanAmount, int loanDuration, Channel channel, String id) throws JAXBException, IOException {
        System.out.println("sending corrId " + id);
        BasicProperties.Builder builder = new BasicProperties.Builder();
        builder.correlationId(id);
        System.out.println(id);
        BasicProperties prop = builder.build();
        Data data = new Data(ssn, loanAmount, loanDuration);
        JAXBContext jc = JAXBContext.newInstance(Data.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        JAXBElement<Data> je2 = new JAXBElement(new QName("Data"), Data.class, data);
        StringWriter sw = new StringWriter();
        marshaller.marshal(je2, sw);
        String xmlString = sw.toString().trim();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput objout = new ObjectOutputStream(bos);
        objout.writeObject(xmlString);
        byte body[] = bos.toByteArray();

        channel.basicPublish(GETBANK_EXCHANGE_NAME, ROUTING_KEY, prop, body);
        System.out.println(" [x] Sent '" + xmlString + "'");

    }

    //unmarshal from string to Object
    private LoanResponse unmarchal(String bodyString) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(LoanResponse.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        StringReader reader = new StringReader(bodyString);
        return (LoanResponse) unmarshaller.unmarshal(reader);
    }

    //marshal from pbkect to xml string
    private String marshal(CustomerResponse d) throws JAXBException {
        JAXBContext jc2 = JAXBContext.newInstance(CustomerResponse.class);
        Marshaller marshaller = jc2.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        JAXBElement<LoanResponse> je2 = new JAXBElement(new QName("Data"), CustomerResponse.class, d);
        StringWriter sw = new StringWriter();
        marshaller.marshal(je2, sw);

        return removeBom(sw.toString());
    }

    //remove unnecessary charactors before xml declaration 
    private String removeBom(String bodyString) {
        String res = bodyString.trim();
        int substringIndex = res.indexOf("<?xml");
        if (substringIndex < 0) {
            return res;
        }
        return res.substring(res.indexOf("<?xml"));
    }

}
