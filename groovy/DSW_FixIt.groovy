def session_map = [:]
def session_id = ''
def event_time = ''
def review_status = ''

// DSW will sometimes send multiple transactions (with the same Session ID) for one order. So, for DSW
// a unique transaction correlates to a unique Session ID. In other words 10 transactions with the same
// Sesson ID should be treated as 1 transaction.
//
// This scripts takes as input a "sorted" (oldest to newest) cleaned events file:
//
//      Event Time,Policy Score,Request ID
//      2016/08/03 00:00:07.261,-10,3684071e607346b5a60882996cec7a0e
//      2016/08/03 00:00:14.954,-10,4951b13626e74336b47772eef27b7f6c
//      2016/08/03 00:00:17.308,0,6b2dc88ce5254521b260e96b6f00c151
//      2016/08/03 00:00:30.084,0,d3d5c96e6cba4269a8d2ba550e204ab0
//      2016/08/03 00:00:51.618,0,16ee77b4339f466ba99dbe51afb98e2c
//      2016/08/03 00:00:54.977,0,b3e46c7964244177aa749ef340a9e0dd
//      2016/08/03 00:00:55.614,-10,e49214838116480aac8b7c7313d4ac4d
//      2016/08/03 00:00:56.914,0,44b1a897e91f456f92b4bc1bdb7606b4
//      2016/08/03 00:01:05.419,-5,a764dee69c8249a48df13d0b5e10c3df
//
// And removes duplicate Session ID transactions. It attempts to return the highest approval level, where multiple
// Sesson IDs exist. So 2 rejects and 1 pass will keep the "pass" transaction.
//
// Note: DSW also wants to exclude Session IDs that begin with "perf1a" (ex: dswco-perf1a1847405388).
//
input_file = args[0]

new File(input_file).eachLine {
    if( (it.contains("perf1a")) || (it.contains("prod1a")) ){
        return
    }
    try{
        // NOTE: modify splits below based on column locations
        session_id = it.split(',')[3]
        event_time = it.split(',')[0]
        review_status = it.split(',')[2]
    }
    catch(ArrayIndexOutOfBoundsException ex){
        return
    }

    if(session_map.containsKey(session_id)){
        // Check the review status
        //Pass
        if(review_status == 'pass'){
            session_map[session_id] = ['pass',event_time]
        }
        //Review
        else if( (review_status == 'review') && (session_map[session_id][0] == 'reject') ){
            session_map[session_id] = ['review',event_time]
        }
        else if( (review_status == 'review') && (session_map[session_id][0] != 'reject') ){
            session_map[session_id][1] = event_time // Update event_time only
        }
        //Reject
        else if(review_status == 'reject'){
            session_map[session_id][1] = event_time // Update event_time only
        }
    }
    else{
        session_map[session_id] = [review_status,event_time]
    }
}

session_map.remove('SESSION_ID')

def first = true
new File(input_file).eachLine {
    if(first){
        println "${it},reviewstatuscalc" 
        first = false
        return
    }
    try{
        // NOTE: modify splits below based on column locations
        event_time = it.split(',')[0]
        session_id = it.split(',')[3]
    }
    catch(ArrayIndexOutOfBoundsException ex){
        return
    }
    

    if(session_map.containsKey(session_id)){
        if(event_time == session_map[session_id][1]){
         println "${it},${session_map[session_id][0]}"
        }
    }
}
