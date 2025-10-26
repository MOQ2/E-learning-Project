import asyncio
import httpx
import os
from typing import Optional, Dict, List

BACKEND_URL = os.environ.get("BACKEND_URL", "http://localhost:5000")


async def fetch_course_details(course_id: int) -> Optional[Dict]:
    """Fetch full course details from the main backend API."""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{BACKEND_URL}/api/courses/{course_id}",
                timeout=10.0
            )
            if response.status_code == 200:
                response_data = response.json()
                # Extract the course data from the nested response structure
                if response_data.get('success') and response_data.get('data'):
                    return response_data['data']
                else:
                    print(f"Invalid response format for course {course_id}: {response_data}")
                    return None
            else:
                print(f"Error fetching course {course_id}: {response.status_code}")
                return None
    except Exception as e:
        print(f"Exception fetching course {course_id}: {e}")
        return None


async def fetch_multiple_courses(course_ids: List[int]) -> List[Dict]:
    """Fetch multiple course details efficiently."""
    courses = []
    async with httpx.AsyncClient() as client:
        tasks = []
        for course_id in course_ids:
            tasks.append(
                client.get(
                    f"{BACKEND_URL}/api/courses/{course_id}",
                    timeout=10.0
                )
            )
        
        responses = await asyncio.gather(*tasks, return_exceptions=True)
        
        for i, response in enumerate(responses):
            if isinstance(response, Exception):
                print(f"Error fetching course {course_ids[i]}: {response}")
                continue
            
            if response.status_code == 200:
                response_data = response.json()
                # Extract the course data from the nested response structure
                if response_data.get('success') and response_data.get('data'):
                    courses.append(response_data['data'])
                else:
                    print(f"Invalid response format for course {course_ids[i]}: {response_data}")
    
    return courses


async def fetch_module_details(module_id: int) -> Optional[Dict]:
    """Fetch module details from the main backend API."""
    try:
        async with httpx.AsyncClient() as client:
            response = await client.get(
                f"{BACKEND_URL}/api/modules/{module_id}",
                timeout=10.0
            )
            if response.status_code == 200:
                response_data = response.json()
                # Extract the module data from the nested response structure
                if response_data.get('success') and response_data.get('data'):
                    return response_data['data']
                else:
                    print(f"Invalid response format for module {module_id}: {response_data}")
                    return None
            else:
                print(f"Error fetching module {module_id}: {response.status_code}")
                return None
    except Exception as e:
        print(f"Exception fetching module {module_id}: {e}")
        return None


def extract_course_content_for_embedding(course_data: Dict) -> str:
    """Extract and format course content for creating embeddings."""
    parts = []
    
    # Course basic info
    if course_data.get('name'):
        parts.append(f"Course: {course_data['name']}")
    
    if course_data.get('description'):
        parts.append(f"Description: {course_data['description']}")
    
    # Category and difficulty
    if course_data.get('category'):
        parts.append(f"Category: {course_data['category']}")
    
    if course_data.get('difficultyLevel'):
        parts.append(f"Difficulty: {course_data['difficultyLevel']}")
    
    # Tags
    if course_data.get('tags'):
        tag_names = [tag.get('name', '') for tag in course_data['tags'] if tag.get('name')]
        if tag_names:
            parts.append(f"Topics: {', '.join(tag_names)}")
    
    # Modules
    if course_data.get('modules'):
        parts.append("\nCourse Content:")
        for module in course_data['modules']:
            if module.get('name'):
                parts.append(f"- Module: {module['name']}")
            if module.get('description'):
                parts.append(f"  {module['description']}")
    
    return "\n".join(parts)
