// alert("I am here !")
const toggleSidebar = () => {

    if($('.sidebar').is(":visible")) {
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%")
    } else {
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%")
    }
};


//search contacts
const search = ()=> {
    // console.log("searching....");
    let query=$("#search-input").val();
    // console.log(query);

    if(query=='') {
        $("#search-result").hide();
    } else {
        // console.log(query);
        //sending request to server
        let url=`http://localhost:8080/search/${query}`;
        fetch(url).then((response)=> {
            return response.json();
        }).then((data)=>{
            //data....
            // console.log(data);
            let text=`<div class='list-group'>`
            data.forEach((contact) => {
                text+=`<a href='/user/${contact.cId}/contact' class='list-group-item list-group-item-action'>.${contact.name}</a>`
            });

            text+=`</div>`;

            $("#search-result").html(text);
            $("#search-result").show();
        });
        $("#search-result").show();
    }
}

//first request: to server to create order
const paymentStart = () => {
    // alert("payment started");
    var amount = $("#payment_field").val();

    //checks
    if(amount == "" || amount == null) {
        alert("Amount is required !!");
        return;
    }

    //Ajax request to send to server to create an order on server
    $.ajax({
        url : '/user/create_order',
        data: JSON.stringify({amount:amount, info:'order_request'}),
        contentType: 'application/json',
        type: 'POST',
        dataType: 'json',
        success: function(response) {
            // * invoked when success
            console.log(response);
            if(response.status=='created') {
                // Open payment form
                let options={
                    // TODO: putting the key of razorPay
                    key: '',
                    amount: response.amount,
                    currency: 'INR',
                    name: 'SCM',
                    description: "Donation",
                    image: "",
                    order_id: response.id,
                    handler: function(response) {
                        console.log(response.razorpay_payment_id);
                        console.log(response.razorpay_order_id);
                        console.log(response.razorpay_signature);
                        console.log("payment successfull");
                        // alert("Congrats! payment successfull");
                        updatePaymentOnServer(
                            respnse.payment_id,
                            response.razorpay_order_id,
                            "paid");
                            
                        swal("Thank you!","Payment successfull","success");
                    },
                    prefill: {
                        name: "",
                        email: "",
                        contact: "",
                    },

                    notes: {
                        address: "India",
                    },

                    theme: {
                        color: "#3399cc",
                    },
                };

                let rzp = new Razorpay(options);

                rzp.on('payment.failed', function(response) {
                    console.log(response.error.code);
                    console.log(response.error.description);
                    console.log(response.error.source);
                    console.log(response.error.step);
                    console.log(response.error.reason);
                    console.log(response.error.metadata.order_id);
                    console.log(response.error.metadata.payment_id);
                    // alert("Oops !! payment failed");
                    swal("Oops!","Payment failed","error");
                });
                rzp.open();
            }
        },
        error: function(error) {
            //invoked when error
            console.log(error);
            swal("Oops!","Payment failed","error");
        },
    });
};

function updatePaymentOnServer(payment_id, order_id, status) {
    $.ajax({
        url: "/user/update_order",
        data: JSON.stringify({
            payment_id: payment_id,
            order_id: order_id,
            status: status,
        }),
        contentType: "application/json",
        type: "POST",
        dataType: "json",
        success:function(respnse) {
            swal("Thank you!","Payment successfull","success");
        },
        error: function(error) {
            swal("Failed !","Payment successfull, but we did not get on server, we will contact you ASAP","error");
        }
    });
}