package booking.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import booking.model.Comment;
import booking.model.Hotel;
import booking.model.Image;
import booking.repository.CategoryRepository;
import booking.repository.HotelRepository;
import booking.repository.ImageRepository;
import booking.repository.RoomRepository;
import booking.repository.RoomTypeRepository;
import booking.repository.UserRepository;
import booking.util.HotelNotFoundException;

/*
 * Mapping
 * GET  /hotels 			- the list of hotels
 * GET  /hotels/new			- the form to fill the data for a new hotel 
 * POST /hotels         	- creates a new hotel
 * GET  /hotels/{id} 		- the hotel with identifier {id}
 * GET  /hotels/{id}/edit 	- the form to edit the hotel with identifier {id}
 * POST /hotels/{id} 	 	- update the hotel with identifier {id}
 * 
 * GET /hotels/{id}/rooms/{id_room} - the room with identifier {id_room} of hotel with identifier {id}
 * GET /hotels/{id}/rooms/new - the form to fill the data for a new room
 * 
 */

@Controller
@RequestMapping(value="/hotels")
public class HotelController {

	@Autowired
	HotelRepository hotels;

	@Autowired
	CategoryRepository categories;

	@Autowired
	RoomTypeRepository roomTypes;

	@Autowired
	RoomRepository rooms;

	@Autowired
	UserRepository users;
	
	@Autowired
	ImageRepository images;


	// GET  /hotels 			- the list of hotels
	@RequestMapping(method=RequestMethod.GET)
	public String index(Model model) {
		model.addAttribute("hotels", hotels.findAll());
		return "hotels/index";
	}

	// GET  /hotels.json 			- the list of hotels
	@RequestMapping(method=RequestMethod.GET, produces={"text/plain","application/json"})
	public @ResponseBody Iterable<Hotel> indexJSON(Model model) {
		return hotels.findAll();
	}

	// GET  /hotels/new			- the form to fill the data for a new hotel
	@RequestMapping(value="/new", method=RequestMethod.GET)
	public String newHotel(Model model) {
		model.addAttribute("hotel", new Hotel());
		model.addAttribute("categories", categories.findAll());
		return "hotels/create";
	}

	// POST /hotels         	- creates a new hotel
	@RequestMapping(method=RequestMethod.POST)
	public String saveIt(@ModelAttribute Hotel hotel, Model model) {
		hotels.save(hotel);
		model.addAttribute("hotel", hotel);
		return "redirect:/hotels";
	}

	// GET  /hotels/{id} 		- the hotel with identifier {id}
	@RequestMapping(value="{id}", method=RequestMethod.GET) 
	public String show(@PathVariable("id") long id, Model model) {
		Hotel hotel = hotels.findOne(id);
		if( hotel == null )
			throw new HotelNotFoundException();
		model.addAttribute("hotel", hotel );
		model.addAttribute("reply", new Comment());
		model.addAttribute("users", users.findAll());
		return "hotels/show";
	}

	// GET  /hotels/{id}.json 		- the hotel with identifier {id}
	@RequestMapping(value="{id}", method=RequestMethod.GET, produces={"text/plain","application/json"})
	public @ResponseBody Hotel showJSON(@PathVariable("id") long id, Model model) {
		Hotel hotel = hotels.findOne(id);
		if( hotel == null )
			throw new HotelNotFoundException();
		return hotel;
	}

	@RequestMapping(value="{id}/edit", method=RequestMethod.GET)
	public String edit(@PathVariable("id") long id, Model model) { 	
		Hotel hotel = hotels.findOne(id);
		model.addAttribute("hotel", hotel);    	
		model.addAttribute("categories", categories.findAll());
		return "hotels/edit";
	}

	// POST /hotels/{id} 	 	- update the hotel with identifier {id}
	@RequestMapping(value="{id}", method=RequestMethod.POST)
	public String editSave(@PathVariable("id") long id, @ModelAttribute("hotel") Hotel hotel) {    	
		hotels.save(hotel);
		return "redirect:/hotels/{id}";
	}

	// GET  /hotels/{id}/remove 	- removes the hotel with identifier {id}
	@RequestMapping(value="{id}/remove", method=RequestMethod.GET)
	public String remove(@PathVariable("id") long id, Model model) {
		hotels.delete(hotels.findOne(id));
		return "redirect:/hotels";
	}  

	@RequestMapping(value="{id}/upload", method=RequestMethod.POST)
	public String uploadImage(@PathVariable("id") long id, Model model,@RequestParam("files") MultipartFile files[] ){
	
		if(files.length > 0)
		{
			for(int i=0; i < files.length; i++){
				
				MultipartFile file = files[i];
				try {
					byte[] bytes = file.getBytes();
					String path = "src/main/resources/public/static/" + file.getOriginalFilename();
					BufferedOutputStream stream =
							new BufferedOutputStream(new FileOutputStream(new File(path)));
					stream.write(bytes);
					stream.close();
					
					Image image = new Image();
					image.setHotel(hotels.findOne(id));
					image.setInsertion_date(new Date());
					image.setPath(file.getOriginalFilename());
					images.save(image);

				} catch (Exception e) {
				}
			}
			return "redirect:/hotels/{id}";
		}
		return "";
	}
}