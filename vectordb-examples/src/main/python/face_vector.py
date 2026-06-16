import sys
import json
from deepface import DeepFace

def main():
    if len(sys.argv) < 2:
        print(json.dumps({"error": "Missing image path parameter"}))
        sys.exit(1)
        
    image_path = sys.argv[1]
    try:
        # Generate the ArcFace facial coordinates vector
        result = DeepFace.represent(img_path=image_path, model_name="ArcFace")
        print(json.dumps({"vector": result}))
    except Exception as e:
        print(json.dumps({"error": str(e)}))
        sys.exit(1)

if __name__ == "__main__":
    main()

