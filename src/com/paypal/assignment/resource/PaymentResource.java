
package com.paypal.assignment.resource;

import java.math.BigDecimal;
import java.sql.Statement;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.google.gson.Gson;
import com.paypal.assignment.dto.CheckoutRequest;
import com.paypal.assignment.dto.ClientTokenResponse;


@Path("/client")
public class PaymentResource {
	private static final String ACCESS_TOKEN="access_token$sandbox$mmhr5nyfpmhnsmyh$c4d0403b96d06186db280fa7e2f7ca0c";
    Statement stmt = null;

    @GET
	@Produces({MediaType.TEXT_PLAIN })
	@Path("/token")
	public Response getClientToken(){
		
		String result=null;
		Gson gs= new Gson();
		String clientToken=null;
		try
		{
			BraintreeGateway gateway = new BraintreeGateway(ACCESS_TOKEN);
			clientToken=gateway.clientToken().generate().toString();
			ClientTokenResponse clientTokenResponse=new ClientTokenResponse();
			clientTokenResponse.setSuccess(true);
			clientTokenResponse.setToken(clientToken);
			result=gs.toJson(clientTokenResponse);
			System.out.println("generated client token  "+clientToken);
						
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(result!=null)
			return Response.ok(result).build();
		else
			return Response.status(Response.Status.ACCEPTED).entity("There is no record").build();

		
		
	}
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
	@Path("/checkout/")
	public Response beginExpressCheckOut(String param){
    	
    	String result=null;
		Gson gs= new Gson();
		Result<Transaction> saleResult=null;
    	try
    	{
    		BraintreeGateway gateway = new BraintreeGateway(ACCESS_TOKEN);
    	
    		CheckoutRequest checkoutRequest=(CheckoutRequest)gs.fromJson(param,CheckoutRequest.class);
    		TransactionRequest request = new TransactionRequest();
    		request.amount(new BigDecimal(checkoutRequest.getAmount())).
    			    merchantAccountId(checkoutRequest.getCurrency()).
    			    paymentMethodNonce(checkoutRequest.getPayment_nonce()).
    			    orderId(checkoutRequest.getOrderId()).
    			    descriptor().
    			      name(checkoutRequest.getOrderDescription()).
    			      done();
    		request.shippingAddress()
    			        .firstName("John")
    			        .lastName("Smith")
    			        .company("Braintree")
    			        .streetAddress(checkoutRequest.getAddress1())
    			        .extendedAddress(checkoutRequest.getAddress2())
    			        .locality("Bartlett")
    			        .region(checkoutRequest.getState())
    			        .postalCode(checkoutRequest.getPostal_code())
    			        .countryCodeAlpha2(checkoutRequest.getCountry())
    			        .done();
    		request. options().
    			      paypal().
    			        customField("PayPal custom field").
    			        description("Description for PayPal email receipt").
    			        done();
    			     
    		System.out.println("checkoutRequest  "+checkoutRequest.getPayment_nonce());
			TransactionRequest transactionRequest=new TransactionRequest();
			transactionRequest.amount( new BigDecimal(checkoutRequest.getAmount()));
			transactionRequest.merchantAccountId(checkoutRequest.getCurrency());
			
			transactionRequest.paymentMethodNonce(checkoutRequest.getPayment_nonce());
			saleResult=gateway.transaction().sale(transactionRequest);
			System.out.println("saleResult  "+saleResult.toString());
			
			if(saleResult.getErrors()!=null)
				System.out.println("saleResult  "+saleResult.getErrors().toString());
			
			result=gs.toJson(saleResult);
			
			System.out.println("result  "+result);
    		
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	if(saleResult.isSuccess())
			return Response.ok(result).build();
		else
			return Response.status(Response.Status.BAD_REQUEST).entity("The transaction failed").build();
    }
    
	
}
