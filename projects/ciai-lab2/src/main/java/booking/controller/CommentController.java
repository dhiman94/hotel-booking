package booking.controller;

import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import booking.model.Comment;
import booking.model.Hotel;
import booking.repository.CommentRepository;
import booking.repository.HotelRepository;
import booking.repository.UserRepository;

@Controller
@RequestMapping(value="/hotels")
public class CommentController {
	
	@Autowired
	HotelRepository hotels;
	
	@Autowired
	CommentRepository comments;
	
	@Autowired
	UserRepository users;
	
	@RequestMapping(value="/{id}/comments/{comment_id}/reply", method = RequestMethod.POST)
	public String createReply(@ModelAttribute Comment reply, @PathVariable("id") long id, 
			Model model, @PathVariable("comment_id") long comment_id){
		
		Hotel hotel = hotels.findOne(id);
		Comment comment = comments.findOne(comment_id);		
		Date date = new Date();
    	reply.setDate(date);
    	reply.setHotel(hotel);
    	reply.setAnswer(true);
    	reply.setStatus(true);
    	comments.save(reply);    
    	comment.setReply(reply);
    	comments.save(comment);
    	return "redirect:/hotels/{id}/comments/";
	}
    
    @RequestMapping(value="/{id}/comments/", method = RequestMethod.POST)
    public String createComment(@ModelAttribute Comment comment, @PathVariable("id") long id, Model model){
    	Hotel hotel = hotels.findOne(id);
    	Date date = new Date();
    	comment.setDate(date);
    	comment.setHotel(hotel);
    	comments.save(comment);    	
    	return "redirect:/hotels/{id}";
    }
    
    @RequestMapping(value="/{id}/comments/new", method = RequestMethod.GET)
    public String newComment(@ModelAttribute Comment comment, @PathVariable("id") long id, Model model){
    	model.addAttribute("comment", new Comment());
    	model.addAttribute("hotel", hotels.findOne(id));
    	model.addAttribute("users", users.findAll());
    	return "comments/create";
    }
    
    @RequestMapping(value="{id}/comments/", method=RequestMethod.GET)
    public String showComments(@PathVariable("id") long id, Model model) {
    	Hotel hotel = hotels.findOne(id);
    	Iterable<Comment> hotel_comments = comments.getComments(id);
    	
    	model.addAttribute("comments", hotel_comments);
    	model.addAttribute("hotel", hotel);
    	model.addAttribute("reply", new Comment());
    	model.addAttribute("users", users.findAll());
    	return "comments/hotel-comments";
    }
    
    @RequestMapping(value="{id}/comments/{id_comment}", method=RequestMethod.GET)
    public String showComment(@PathVariable("id") long id, @PathVariable("id_comment") long id_comment, Model model) {
    	Hotel hotel = hotels.findOne(id);
    	model.addAttribute("hotel", hotel);
    	model.addAttribute("comment", hotel.getComments().get(id_comment));
    	return "comments/show";
    }
    
    @RequestMapping(value="{id}/comments/{id_comment}/edit", method=RequestMethod.GET)
    public String editComment(@PathVariable("id") long id, @PathVariable("id_comment") long id_comment, Model model) {
    	
    	Hotel hotel = hotels.findOne(id);
    	model.addAttribute("hotel", hotel);
    	model.addAttribute("comment", hotel.getComments().get(id_comment));
    	return "comments/edit";
    }
    
    @RequestMapping(value="{id}/comments/{id_comment}/remove", method=RequestMethod.GET)
    public String removeComment(@PathVariable("id") long id, @PathVariable("id_comment") long id_comment, Model model){    	
    	comments.delete(id_comment);
		return "redirect:/hotels/{id}/comments/";	
    }
    
    @RequestMapping(value="{id}/comments/{id_comment}/approve", method=RequestMethod.GET)
    public String approveComment(@PathVariable("id") long id, @PathVariable("id_comment") long id_comment, Model model) {    	
    	Comment comment = comments.findOne(id_comment);
    	comment.setStatus(true);
    	comments.save(comment);
    	return "redirect:/hotels/{id}/comments/";
    }
}
